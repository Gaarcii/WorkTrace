package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.service.files.AdminPdfGeneratorService;
import com.worktrace.worktracebackend.service.files.EmployeePdfGeneratorService;
import com.worktrace.worktracebackend.service.files.ExcelGeneratorService;
import com.worktrace.worktracebackend.service.auditTimeEntry.AuditTimeEntryService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.incidence.IncidenceService;
import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserService userService;
    private final IpDetectionService ipDetectionService;
    private final WorkScheduleRepository workScheduleRepository;
    private final IncidenceRepository incidentRepository;
    private final EmployeePdfGeneratorService employeePdfGeneratorService;
    private final AdminPdfGeneratorService adminPdfGeneratorService;
    private final ExcelGeneratorService excelGeneratorService;
    private final IncidenceService incidenceService;
    private final ObjectMapper objectMapper;
    private final AuditTimeEntryService auditTimeEntryService;

    @Transactional
    public TimeEntryResponseDto processTimeEntry(TimeEntryRequestDto requestDto, String realIp, String userAgent) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        User user = info.getUser();
        Company company = info.getCompany();
        Profile profile = info.getProfile();

        Optional<TimeEntry> openTimeEntryOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(profile.getUserId(), TimeEntryStatus.OPEN);

        IpDetectionService.IpAnalysisResult ipResult = ipDetectionService.
                analyzeIpWithDetails(realIp);

        List<String> flags = new ArrayList<>();
        if (requestDto.getAccuracyMeters() != null && requestDto.getAccuracyMeters() > 200) {
            flags.add("LOW_GPS_ACCURACY");
        }
        if (ipResult.flags() != null) {
            flags.addAll(ipResult.flags());
        }

        TimeEntry savedTimeEntry;

        if (openTimeEntryOpt.isPresent()) {
            TimeEntry openTimeEntry = openTimeEntryOpt.get();

            openTimeEntry.setEndAt(OffsetDateTime.now());
            openTimeEntry.setEndLat(requestDto.getLat());
            openTimeEntry.setEndLng(requestDto.getLng());
            openTimeEntry.setEndAccuracyM(requestDto.getAccuracyMeters());
            openTimeEntry.setEndIp(realIp);
            openTimeEntry.setEndUserAgent(userAgent);
            openTimeEntry.setEndGeoip(ipResult.geoIpMap());

            if (openTimeEntry.getFlags() != null) {
                openTimeEntry.getFlags().addAll(flags);
            } else {
                openTimeEntry.setFlags(flags);
            }

            openTimeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
            savedTimeEntry = timeEntryRepository.save(openTimeEntry);

        } else {
            TimeEntry newTimeEntry = new TimeEntry();
            newTimeEntry.setEmployee(profile);
            newTimeEntry.setCompany(company);
            newTimeEntry.setCreatedBy(user);

            newTimeEntry.setWorkDate(LocalDate.now());
            newTimeEntry.setStartAt(OffsetDateTime.now());
            newTimeEntry.setCreatedAt(OffsetDateTime.now());

            newTimeEntry.setStartLat(requestDto.getLat());
            newTimeEntry.setStartLng(requestDto.getLng());
            newTimeEntry.setStartAccuracyM(requestDto.getAccuracyMeters());
            newTimeEntry.setStartIp(realIp);
            newTimeEntry.setStartUserAgent(userAgent);
            newTimeEntry.setStartGeoip(ipResult.geoIpMap());

            newTimeEntry.setFlags(flags);
            newTimeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);

            savedTimeEntry = timeEntryRepository.save(newTimeEntry);
        }

        TimeEntryResponseDto response = new TimeEntryResponseDto();
        response.setId(savedTimeEntry.getId());
        response.setStartAt(savedTimeEntry.getStartAt());
        response.setEndAt(savedTimeEntry.getEndAt());
        response.setStatus(savedTimeEntry.getTimeEntryStatus().name());

        return response;
    }

    @Transactional(readOnly = true)
    public DailySummaryResponseDto getDailySummary() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        DailySummaryResponseDto dailySummary = calculateDailySummary(info.getUser(), info.getProfile(), LocalDate.now());

        List<TimeEntry> latestTimeEntriesList = timeEntryRepository.findTop5ByEmployee_UserIdOrderByStartAtDesc(info.getUser().getId());
        List<LastTimeEntriesResponseDto> latest5TimeEntries = mapTimeEntriesToDto(latestTimeEntriesList).stream()
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .limit(5)
                .toList();
        dailySummary.setLastTimeEntries(latest5TimeEntries);

        return dailySummary;
    }

    @Transactional(readOnly = true)
    public HistoryResponseDto getHistoryByDate(LocalDate date) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        DailySummaryResponseDto dailySummary = calculateDailySummary(info.getUser(), info.getProfile(), date);

        List<TimeEntry> dailyTimeEntries = timeEntryRepository.
                findTimeEntriesByEmployee_UserIdAndWorkDate(info.getUser().getId(), date);

        LocalDate startDate = date.with(DayOfWeek.MONDAY);
        LocalDate endDate = date.with(DayOfWeek.SUNDAY);

        Long weeklyWorkedMinutes = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDateRange(info.getProfile().getUserId(), startDate, endDate);

        Long weeklyTargetMinutes = info.getProfile().getWeeklyHours() != null
                ? info.getProfile().getWeeklyHours().multiply(BigDecimal.valueOf(60)).longValue()
                : 0L;

        List<LastTimeEntriesResponseDto> dailyRecords = mapTimeEntriesToDto(dailyTimeEntries).stream()
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .toList();
        HistoryResponseDto historyResponseDto = new HistoryResponseDto();
        historyResponseDto.setDailyTargetMinutes(dailySummary.getTargetMinutes());
        historyResponseDto.setDailyWorkedMinutes(dailySummary.getAccumulatedMinutes());
        historyResponseDto.setDailyRecords(dailyRecords);
        historyResponseDto.setWeeklyTargetMinutes(weeklyTargetMinutes);
        historyResponseDto.setWeeklyWorkedMinutes(weeklyWorkedMinutes);
        return historyResponseDto;
    }


    private List<LastTimeEntriesResponseDto> mapTimeEntriesToDto(List<TimeEntry> timeEntries) {
        List<LastTimeEntriesResponseDto> dtos = new ArrayList<>();
        for (TimeEntry timeEntry : timeEntries) {
            dtos.add(new LastTimeEntriesResponseDto(
                    timeEntry.getId(), "Entrada", timeEntry.getStartAt()));
            if (timeEntry.getEndAt() != null) {
                dtos.add(new LastTimeEntriesResponseDto(
                        timeEntry.getId(), "Salida", timeEntry.getEndAt()));
            }
        }
        return dtos;
    }

    private DailySummaryResponseDto calculateDailySummary(User user, Profile profile, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Optional<TimeEntry> currentTimeEntryOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(user.getId(), TimeEntryStatus.OPEN);

        Long accumulatedMinutes = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDate(user.getId(), date);

        Optional<WorkSchedule> scheduleOpt = workScheduleRepository
                .findByEmployee_UserIdAndDayOfWeek(profile.getUserId(), dayOfWeek);

        Duration targetDuration;
        if (scheduleOpt.isPresent()) {
            LocalTime start = scheduleOpt.get().getStartTime();
            LocalTime end = scheduleOpt.get().getEndTime();

            targetDuration = Duration.between(start, end);

            if (targetDuration.isNegative()) {
                targetDuration = targetDuration.plusDays(1);
            }
        } else {
            targetDuration = Duration.ofMinutes(0);
        }

        DailySummaryResponseDto dailySummary = new DailySummaryResponseDto();
        dailySummary.setEntryTime(currentTimeEntryOpt.map(TimeEntry::getStartAt).orElse(null));
        dailySummary.setAccumulatedMinutes(accumulatedMinutes);
        dailySummary.setTargetMinutes(targetDuration.toMinutes());

        return dailySummary;
    }

    public StatisticsResponseDto getStatistics(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        StatisticsResponseDto responseDto = new StatisticsResponseDto();

        List<WorkSchedule> schedulesList = workScheduleRepository
                .findByEmployee_UserId(info.getProfile().getUserId());

        Map<DayOfWeek, Long> dailyTargetMinutes = new EnumMap<>(DayOfWeek.class);
        for (WorkSchedule schedule : schedulesList) {
            Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
            if (duration.isNegative()) {
                duration = duration.plusDays(1);
            }
            dailyTargetMinutes.put(DayOfWeek.valueOf(
                    schedule.getDayOfWeek().toString()), duration.toMinutes());
        }

        List<DailyStatisticsProjection> dailyStatistics = timeEntryRepository
                .getGroupedDailyStatistics(info.getProfile().getUserId(), startDate, endDate);

        Map<LocalDate, Long> workedMinutesByDate = dailyStatistics.stream()
                .collect(Collectors.toMap(
                        DailyStatisticsProjection::getFecha,
                        DailyStatisticsProjection::getMinutosTrabajados
                ));

        List<WorkerIncidenceResponseDto> incidencesInRange = incidenceService
                .getIncidencesByDateRange(startDate, endDate);

        long totalWorkedMinutes = 0L;
        long totalMinutesBalance = 0L;
        int incompleteWorkdays = 0;

        LocalDate currentDate = startDate;
        List<DailyStatisticDto> dailySummaries = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            long plannedMinutes = dailyTargetMinutes.getOrDefault(
                    currentDate.getDayOfWeek(), 0L);
            long workedMinutes = workedMinutesByDate.getOrDefault(
                    currentDate, 0L);

            DailyStatisticDto dailyStatisticDto = new DailyStatisticDto();
            dailyStatisticDto.setDate(currentDate);
            dailyStatisticDto.setPlannedMinutes(plannedMinutes);
            dailyStatisticDto.setWorkedMinutes(workedMinutes);

            totalWorkedMinutes += workedMinutes;
            totalMinutesBalance += (workedMinutes - plannedMinutes);

            if (plannedMinutes > 0 && workedMinutes < plannedMinutes) {
                incompleteWorkdays++;
            }

            dailySummaries.add(dailyStatisticDto);

            currentDate = currentDate.plusDays(1);
        }

        int totalIncidences = incidentRepository.countIncidentsByUserAndDates(
                info.getUser().getId(),
                startDate,
                endDate
        );

        responseDto.setTotalWorkedMinutes(totalWorkedMinutes);
        responseDto.setMinutesBalance(totalMinutesBalance);
        responseDto.setIncompleteWorkdays(incompleteWorkdays);
        responseDto.setIncidencesCount(totalIncidences);
        responseDto.setDailySummary(dailySummaries);
        responseDto.setIncidenceList(incidencesInRange);

        return responseDto;
    }

    @Transactional(readOnly = true)
    public byte[] exportEmployeeHistoryPdf(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
                        info.getProfile().getUserId(),
                        startDate,
                        endDate
                );

        return employeePdfGeneratorService.generateTimeEntriesPdf(
                info.getProfile(),
                info.getUser(),
                timeEntries,
                startDate,
                endDate
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveWorkerDto> getActiveWorkers() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntryList = timeEntryRepository
                .getAllByCompany_IdAndEndAtIsNull(company.getId());

        ZoneId zone = ZoneId.of("Europe/Madrid");

        return timeEntryList.stream()
                .map(timeEntry -> {
                    Profile employee = timeEntry.getEmployee();

                    String jobPositionTitle = null;
                    if (employee.getPosition() != null) {
                        jobPositionTitle = employee.getPosition().getTitle();
                    }

                    Long punctuality = null;
                    OffsetDateTime startAt = timeEntry.getStartAt();

                    if (startAt != null) {
                        ZonedDateTime localEntryTime = startAt.atZoneSameInstant(zone);
                        DayOfWeek localDay = localEntryTime.getDayOfWeek();

                        Optional<WorkSchedule> scheduleOpt = workScheduleRepository
                                .findByEmployee_UserIdAndDayOfWeek(employee.getUserId(), localDay);

                        if (scheduleOpt.isPresent()) {
                            LocalTime scheduleTime = scheduleOpt.get().getStartTime();
                            LocalTime localEntryTimeOnly = localEntryTime.toLocalTime();

                            punctuality = Duration.between(scheduleTime, localEntryTimeOnly).toMinutes();
                        }
                    }

                    return new ActiveWorkerDto(
                            employee.getUserId(),
                            employee.getFullName(),
                            jobPositionTitle,
                            employee.getAvatarUrl(),
                            timeEntry.getStartAt(),
                            punctuality
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getTimeEntriesCountToday() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        return timeEntryRepository.countAllByCompany_IdAndWorkDateBetween(
                company.getId(), LocalDate.now(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public TotalHoursTodayResponseDto getTotalHoursToday() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        Long totalMinutes = timeEntryRepository
                .getWorkedMinutesByCompanyAndDate(company.getId(), LocalDate.now());

        if (totalMinutes == null) {
            totalMinutes = 0L;
        }

        return new TotalHoursTodayResponseDto(totalMinutes);
    }

    @Transactional(readOnly = true)
    public List<DailyTimeEntryCountDto> getWeeklyTimeEntryCountChartData(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<DailyTimeEntryCountProjection> timeEntryCounts = timeEntryRepository
                .getTimeEntryCountByCompanyAndDateRange(
                        company.getId(), startDate, endDate);

        Map<LocalDate, Long> countByDateMap = new HashMap<>();
        for (DailyTimeEntryCountProjection projection : timeEntryCounts) {
            countByDateMap.put(projection.getFecha(), projection.getNumFichajes());
        }

        List<DailyTimeEntryCountDto> result = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            Long timeEntryCount = countByDateMap.getOrDefault(date, 0L);
            result.add(new DailyTimeEntryCountDto(date.toString(), timeEntryCount));
            date = date.plusDays(1);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public LocalDate getFirstTimeEntryDateForEmployee() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        LocalDate firstDate = timeEntryRepository.findFirstWorkDateByEmployee
                (info.getProfile().getUserId());
        return firstDate != null ? firstDate : LocalDate.now();
    }

    @Transactional
    public void updateTimeEntry(UUID id, EditTimeEntryRequestDto dto) {
        if (dto.getJustification() == null || dto.getJustification().trim().length() < 10) {
            throw new IllegalArgumentException("La justificación es obligatoria y debe tener al menos 10 caracteres.");
        }

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        TimeEntry timeEntry = findAndValidateTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setStartAt(dto.getStartAt());
            timeEntry.setWorkDate(dto.getStartAt().toLocalDate());

            if (dto.getEndAt() != null) {
                timeEntry.setEndAt(dto.getEndAt());
                timeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
            } else {
                timeEntry.setEndAt(null);
                timeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);
            }

            timeEntry.setModificationReason(dto.getJustification());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("ADMIN_ADJUST", dto.getJustification(), oldDataJson, newDataJson, timeEntry.getId());

        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    @Transactional
    public void voidTimeEntry(UUID id, VoidTimeEntryRequestDto dto) {
        if (dto.getJustification() == null || dto.getJustification().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio y debe tener al menos 10 caracteres.");
        }

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        TimeEntry timeEntry = findAndValidateTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setDeletedAt(OffsetDateTime.now());
            timeEntry.setDeletedBy(info.getUser());
            timeEntry.setDeleteReason(dto.getJustification());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("SOFT_DELETE", dto.getJustification(), oldDataJson, newDataJson, timeEntry.getId());
        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<TimeEntryTableResponseDto> getTimeEntriesByEmployee
            (UUID employeeId, Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        User employee = userService.getUserById(employeeId);
        if (!employee.getCompany().getId().equals(info.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        Page<TimeEntry> page = timeEntryRepository
                .findByEmployee_UserIdAndDeletedAtIsNullOrderByWorkDateDesc(employeeId, pageable);

        return page.map(f -> {
            Long workedMinutes = null;

            if (f.getStartAt() != null && f.getEndAt() != null) {
                Duration duration = Duration.between(f.getStartAt(), f.getEndAt());
                workedMinutes = duration.toMinutes();
            }

            return new TimeEntryTableResponseDto(
                    f.getId(),
                    f.getWorkDate(),
                    f.getStartAt(),
                    f.getEndAt(),
                    f.getStartLat(),
                    f.getStartLng(),
                    f.getEndLat(),
                    f.getEndLng(),
                    workedMinutes
            );
        });
    }

    @Transactional(readOnly = true)
    public List<AdminTimeEntryByDateResponseDto> getTimeEntriesByDateForCompany(LocalDate date) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateOrderByStartAtDesc(company.getId(), date);

        return timeEntries.stream().map(timeEntry -> {
            Long workedMinutes = null;
            if (timeEntry.getStartAt() != null && timeEntry.getEndAt() != null) {
                workedMinutes = Duration.between(timeEntry.getStartAt(), timeEntry.getEndAt()).toMinutes();
            }

            Profile employee = timeEntry.getEmployee();
            String jobPositionTitle = employee.getPosition() != null ? employee.getPosition().getTitle() : null;

            return new AdminTimeEntryByDateResponseDto(
                    timeEntry.getId(),
                    employee.getUserId(),
                    employee.getFullName(),
                    jobPositionTitle,
                    employee.getAvatarUrl(),
                    timeEntry.getWorkDate(),
                    timeEntry.getStartAt(),
                    timeEntry.getEndAt(),
                    workedMinutes
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanyReportAsPdf(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return adminPdfGeneratorService.generateCompanyTimeEntriesPdf(company, timeEntries, startDate, endDate, auditRecords);
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanyReportAsExcel(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return excelGeneratorService.generateCompanyTimeEntriesExcel(timeEntries, auditRecords, startDate, endDate);
    }

    private TimeEntry findAndValidateTimeEntry(UUID id, Company company) {
        Optional<TimeEntry> timeEntryOpt = timeEntryRepository.findById(id);
        if (timeEntryOpt.isEmpty()) {
            throw new NotFoundException("No se encontró el fichajeOp");
        }
        if (!timeEntryOpt.get().getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El fichajeOp no pertenece a tu empresa");
        }
        return timeEntryOpt.get();
    }

}
