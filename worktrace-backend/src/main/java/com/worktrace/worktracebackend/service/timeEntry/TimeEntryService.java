package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.auditTimeEntry.AuditRecordDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.service.archivos.AdminPdfGeneratorService;
import com.worktrace.worktracebackend.service.archivos.EmployeePdfGeneratorService;
import com.worktrace.worktracebackend.service.archivos.ExcelGeneratorService;
import com.worktrace.worktracebackend.service.auditTimeEntry.AuditTimeEntryService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
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
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        User user = info.getUser();
        Company company = info.getCompany();
        Profile profile = info.getProfile();

        Optional<TimeEntry> openTimeEntryOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndEstadoFichaje(profile.getUserId(), EstadoFichaje.OPEN);

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

            openTimeEntry.setEstadoFichaje(EstadoFichaje.CLOSED);
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
            newTimeEntry.setEstadoFichaje(EstadoFichaje.OPEN);

            savedTimeEntry = timeEntryRepository.save(newTimeEntry);
        }

        TimeEntryResponseDto response = new TimeEntryResponseDto();
        response.setId(savedTimeEntry.getId());
        response.setStartAt(savedTimeEntry.getStartAt());
        response.setEndAt(savedTimeEntry.getEndAt());
        response.setStatus(savedTimeEntry.getEstadoFichaje().name());

        return response;
    }

    @Transactional(readOnly = true)
    public DailySummaryResponseDto getDailySummary() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        DailySummaryResponseDto dailySummary = calculateDailyData(info.getUser(), info.getProfile(), LocalDate.now());

        List<TimeEntry> latestTimeEntriesList = timeEntryRepository.findTop5ByEmployee_UserIdOrderByStartAtDesc(info.getUser().getId());
        List<UltimosFichajesResponseDto> latest5TimeEntries = mapTimeEntriesToEvents(latestTimeEntriesList).stream()
                .sorted((e1, e2) -> e2.getFecha().compareTo(e1.getFecha()))
                .limit(5)
                .toList();
        dailySummary.setUltimosFichajes(latest5TimeEntries);

        return dailySummary;
    }

    @Transactional(readOnly = true)
    public HistoryResponseDto getHistoryByDate(LocalDate date) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        DailySummaryResponseDto dailySummary = calculateDailyData(info.getUser(), info.getProfile(), date);

        List<TimeEntry> dailyTimeEntries = timeEntryRepository.
                findTimeEntriesByEmployee_UserIdAndWorkDate(info.getUser().getId(), date);

        LocalDate startDate = date.with(DayOfWeek.MONDAY);
        LocalDate endDate = date.with(DayOfWeek.SUNDAY);

        Long weeklyWorkedMinutes = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDateRange(info.getProfile().getUserId(), startDate, endDate);

        Long weeklyTargetMinutes = info.getProfile().getWeeklyHours() != null
                ? info.getProfile().getWeeklyHours().multiply(BigDecimal.valueOf(60)).longValue()
                : 0L;

        List<UltimosFichajesResponseDto> dailyRecords = mapTimeEntriesToEvents(dailyTimeEntries).stream()
                .sorted((e1, e2) -> e2.getFecha().compareTo(e1.getFecha()))
                .toList();
        HistoryResponseDto historyResponseDto = new HistoryResponseDto();
        historyResponseDto.setMinutosObjetivoDia(dailySummary.getMinutosObjetivo());
        historyResponseDto.setMinutosTrabajadosDia(dailySummary.getMinutosAcumulados());
        historyResponseDto.setRegistrosDia(dailyRecords);
        historyResponseDto.setMinutosObjetivoSemana(weeklyTargetMinutes);
        historyResponseDto.setMinutosTrabajadosSemana(weeklyWorkedMinutes);
        return historyResponseDto;
    }


    private List<UltimosFichajesResponseDto> mapTimeEntriesToEvents(List<TimeEntry> timeEntries) {
        List<UltimosFichajesResponseDto> events = new ArrayList<>();
        for (TimeEntry timeEntry : timeEntries) {
            events.add(new UltimosFichajesResponseDto(
                    timeEntry.getId(), "Entrada", timeEntry.getStartAt()));
            if (timeEntry.getEndAt() != null) {
                events.add(new UltimosFichajesResponseDto(
                        timeEntry.getId(), "Salida", timeEntry.getEndAt()));
            }
        }
        return events;
    }

    private DailySummaryResponseDto calculateDailyData(User user, Profile profile, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Optional<TimeEntry> currentTimeEntryOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndEstadoFichaje(user.getId(), EstadoFichaje.OPEN);

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
        dailySummary.setHoraEntrada(currentTimeEntryOpt.map(TimeEntry::getStartAt).orElse(null));
        dailySummary.setMinutosAcumulados(accumulatedMinutes);
        dailySummary.setMinutosObjetivo(targetDuration.toMinutes());

        return dailySummary;
    }

    public StatisticsResponseDto getStatistics(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        StatisticsResponseDto responseDto = new StatisticsResponseDto();

        List<WorkSchedule> schedulesList = workScheduleRepository
                .findByEmployee_UserId(info.getProfile().getUserId());

        Map<DayOfWeek, Long> dailyTargetMinutesMap = new EnumMap<>(DayOfWeek.class);
        for (WorkSchedule schedule : schedulesList) {
            Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
            if (duration.isNegative()) {
                duration = duration.plusDays(1);
            }
            dailyTargetMinutesMap.put(DayOfWeek.valueOf(
                    schedule.getDayOfWeek().toString()), duration.toMinutes());
        }

        List<EstadisticaDiariaProjection> groupedRecords = timeEntryRepository
                .getEstadisticasDiariasAgrupadas(info.getProfile().getUserId(), startDate, endDate);

        Map<LocalDate, Long> workedMinutesByDateMap = groupedRecords.stream()
                .collect(Collectors.toMap(
                        EstadisticaDiariaProjection::getFecha,
                        EstadisticaDiariaProjection::getMinutosTrabajados
                ));

        List<WorkerIncidenceResponseDto> incidences = incidenceService
                .getIncidenciaPorFechas(startDate, endDate);

        long totalWorkedMinutes = 0L;
        long totalBalanceMinutes = 0L;
        int incompleteDays = 0;

        LocalDate currentDate = startDate;
        List<EstadisticaDiariaDto> dailySummaries = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            long plannedMinutes = dailyTargetMinutesMap.getOrDefault(
                    currentDate.getDayOfWeek(), 0L);
            long workedMinutes = workedMinutesByDateMap.getOrDefault(
                    currentDate, 0L);

            EstadisticaDiariaDto dailyStatisticDto = new EstadisticaDiariaDto();
            dailyStatisticDto.setFecha(currentDate);
            dailyStatisticDto.setMinutosPrevistos(plannedMinutes);
            dailyStatisticDto.setMinutosTrabajados(workedMinutes);

            totalWorkedMinutes += workedMinutes;
            totalBalanceMinutes += (workedMinutes - plannedMinutes);

            if (plannedMinutes > 0 && workedMinutes < plannedMinutes) {
                incompleteDays++;
            }

            dailySummaries.add(dailyStatisticDto);

            currentDate = currentDate.plusDays(1);
        }

        int totalIncidences = incidentRepository.countIncidentsByUsuarioYFechas(
                info.getUser().getId(),
                startDate,
                endDate
        );

        responseDto.setMinutosTrabajadosTotal(totalWorkedMinutes);
        responseDto.setBalanceMinutos(totalBalanceMinutes);
        responseDto.setJornadasIncompletas(incompleteDays);
        responseDto.setIncidencias(totalIncidences);
        responseDto.setResumenDiario(dailySummaries);
        responseDto.setIncidenciasList(incidences);

        return responseDto;
    }

    @Transactional(readOnly = true)
    public byte[] exportHistoryPdf(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
                        info.getProfile().getUserId(),
                        startDate,
                        endDate
                );

        return employeePdfGeneratorService.generarPDFFichajes(
                info.getProfile(),
                info.getUser(),
                timeEntries,
                startDate,
                endDate
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveWorkerDto> getActiveWorkers() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> timeEntryList = timeEntryRepository
                .getAllByCompany_IdAndEndAtIsNull(company.getId());

        ZoneId zone = ZoneId.of("Europe/Madrid");

        return timeEntryList.stream()
                .map(timeEntry -> {
                    Profile employee = timeEntry.getEmployee();

                    String jobPosition = null;
                    if (employee.getPosition() != null) {
                        jobPosition = employee.getPosition().getTitle();
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
                            jobPosition,
                            employee.getAvatarUrl(),
                            timeEntry.getStartAt(),
                            punctuality
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getTimeEntriesCountToday() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        return timeEntryRepository.countAllByCompany_IdAndWorkDateBetween(
                company.getId(), LocalDate.now(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public TotalHoursTodayResponseDto getTotalHoursToday() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        Long totalMinutes = timeEntryRepository
                .getWorkedMinutesByCompanyAndDate(company.getId(), LocalDate.now());

        if (totalMinutes == null) {
            totalMinutes = 0L;
        }

        return new TotalHoursTodayResponseDto(totalMinutes);
    }

    @Transactional(readOnly = true)
    public List<DailyTimeEntryCountDto> getWeeklyChartData(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<DailyFichajeCountProjection> groupedTimeEntries = timeEntryRepository
                .getFichajesCountByCompanyAndDateRange(
                        company.getId(), startDate, endDate);

        Map<LocalDate, Long> countByDate = new HashMap<>();
        for (DailyFichajeCountProjection projection : groupedTimeEntries) {
            countByDate.put(projection.getFecha(), projection.getNumFichajes());
        }

        List<DailyTimeEntryCountDto> result = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            Long numTimeEntries = countByDate.getOrDefault(date, 0L);
            result.add(new DailyTimeEntryCountDto(date.toString(), numTimeEntries));
            date = date.plusDays(1);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public LocalDate getFirstTimeEntryDate() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        LocalDate firstDate = timeEntryRepository.findFirstWorkDateByEmployee
                (info.getProfile().getUserId());
        return firstDate != null ? firstDate : LocalDate.now();
    }

    @Transactional
    public void updateTimeEntry(UUID id, EditTimeEntryRequestDto dto) {
        if (dto.getJustificacion() == null || dto.getJustificacion().trim().length() < 10) {
            throw new IllegalArgumentException("La justificación es obligatoria y debe tener al menos 10 caracteres.");
        }

        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        TimeEntry timeEntry = checkTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setStartAt(dto.getEntrada());
            timeEntry.setWorkDate(dto.getEntrada().toLocalDate());

            if (dto.getSalida() != null) {
                timeEntry.setEndAt(dto.getSalida());
                timeEntry.setEstadoFichaje(EstadoFichaje.CLOSED);
            } else {
                timeEntry.setEndAt(null);
                timeEntry.setEstadoFichaje(EstadoFichaje.OPEN);
            }

            timeEntry.setModificationReason(dto.getJustificacion());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("ADMIN_ADJUST", dto.getJustificacion(), oldDataJson, newDataJson, timeEntry.getId());

        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    @Transactional
    public void voidTimeEntry(UUID id, VoidTimeEntryRequestDto dto) {
        if (dto.getJustificacion() == null || dto.getJustificacion().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio y debe tener al menos 10 caracteres.");
        }

        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        TimeEntry timeEntry = checkTimeEntry(id, company);

        try {
            String oldDataJson = objectMapper.writeValueAsString(timeEntry);

            timeEntry.setDeletedAt(OffsetDateTime.now());
            timeEntry.setDeletedBy(info.getUser());
            timeEntry.setDeleteReason(dto.getJustificacion());
            timeEntry.setUpdatedAt(OffsetDateTime.now());

            String newDataJson = objectMapper.writeValueAsString(timeEntry);

            auditTimeEntryService.logTimeEntryChange("SOFT_DELETE", dto.getJustificacion(), oldDataJson, newDataJson, timeEntry.getId());
        } catch (JacksonException e) {
            throw new RuntimeException("Error al generar los datos de auditoría", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<TimeEntryTableResponseDto> getTimeEntriesByEmployeePaginated
            (UUID employeeId, Pageable pageable) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        User employee = userService.getUserById(employeeId);
        if (!employee.getCompany().getId().equals(info.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        Page<TimeEntry> page = timeEntryRepository
                .findByEmployee_UserIdAndDeletedAtIsNullOrderByWorkDateDesc(employeeId, pageable);

        return page.map(f -> {
            Long workedHours = null;

            if (f.getStartAt() != null && f.getEndAt() != null) {
                Duration duration = Duration.between(f.getStartAt(), f.getEndAt());
                workedHours = duration.toMinutes();
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
                    workedHours
            );
        });
    }

    @Transactional(readOnly = true)
    public List<AdminTimeEntryByDateResponseDto> getTimeEntriesByDateForCompany(LocalDate date) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateOrderByStartAtDesc(company.getId(), date);

        return timeEntries.stream().map(timeEntry -> {
            Long workedMinutes = null;
            if (timeEntry.getStartAt() != null && timeEntry.getEndAt() != null) {
                workedMinutes = Duration.between(timeEntry.getStartAt(), timeEntry.getEndAt()).toMinutes();
            }

            Profile employeeProfile = timeEntry.getEmployee();
            String jobPosition = employeeProfile.getPosition() != null ? employeeProfile.getPosition().getTitle() : null;

            return new AdminTimeEntryByDateResponseDto(
                    timeEntry.getId(),
                    employeeProfile.getUserId(),
                    employeeProfile.getFullName(),
                    jobPosition,
                    employeeProfile.getAvatarUrl(),
                    timeEntry.getWorkDate(),
                    timeEntry.getStartAt(),
                    timeEntry.getEndAt(),
                    workedMinutes
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanyReportPdf(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return adminPdfGeneratorService.generarPDFFichajesEmpresa(company, timeEntries, startDate, endDate, auditRecords);
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanyReportExcel(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), startDate, endDate);

        List<AuditRecordDto> auditRecords = List.of();

        return excelGeneratorService.generarExcelFichajesEmpresa(timeEntries, auditRecords, startDate, endDate);
    }

    private TimeEntry checkTimeEntry(UUID id, Company company) {
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
