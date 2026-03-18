package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.ResumenDiarioResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.TimeEntryRequestDto;
import com.worktrace.worktracebackend.dto.timeEntry.TimeEntryResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.UltimosFichajesResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserService userService;
    private final IpDetectionService ipDetectionService;
    private final WorkScheduleRepository workScheduleRepository;

    private UsuarioYCompaniaInfo extraerUsuarioYCompania() {
        assert userService != null;
        User user = userService.getAuthenticatedUser();
        Company company = user.getCompany();
        Profile profile = user.getProfile();
        return new UsuarioYCompaniaInfo(user, company, profile);
    }

    @Transactional
    public TimeEntryResponseDto procesarFichaje(TimeEntryRequestDto requestDto, String ip, String userAgent) {
        UsuarioYCompaniaInfo info = extraerUsuarioYCompania();
        User user = info.getUser();
        Company company = info.getCompany();
        Profile profile = info.getProfile();

        Optional<TimeEntry> turnoAbiertoOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndStatus(profile.getUserId(), Status.OPEN);

        IpDetectionService.IpAnalysisResult ipResult = ipDetectionService.
                analyzeIpWithDetails(ip);

        List<String> flags = new ArrayList<>();
        if (requestDto.getAccuracyMeters() != null && requestDto.getAccuracyMeters() > 200) {
            flags.add("LOW_GPS_ACCURACY");
        }
        if (ipResult.flags() != null) {
            flags.addAll(ipResult.flags());
        }

        TimeEntry fichajeGuardado;

        if (turnoAbiertoOpt.isPresent()) {
            TimeEntry turnoAbierto = turnoAbiertoOpt.get();

            turnoAbierto.setEndAt(OffsetDateTime.now());
            turnoAbierto.setEndLat(requestDto.getLat());
            turnoAbierto.setEndLng(requestDto.getLng());
            turnoAbierto.setEndAccuracyM(requestDto.getAccuracyMeters());
            turnoAbierto.setEndIp(ip);
            turnoAbierto.setEndUserAgent(userAgent);
            turnoAbierto.setEndGeoip(ipResult.geoIpMap());

            if (turnoAbierto.getFlags() != null) {
                turnoAbierto.getFlags().addAll(flags);
            } else {
                turnoAbierto.setFlags(flags);
            }

            turnoAbierto.setStatus(Status.CLOSED);
            fichajeGuardado = timeEntryRepository.save(turnoAbierto);

        } else {
            TimeEntry nuevoFichaje = new TimeEntry();
            nuevoFichaje.setEmployee(profile);
            nuevoFichaje.setCompany(company);
            nuevoFichaje.setCreatedBy(user);

            nuevoFichaje.setWorkDate(LocalDate.now());
            nuevoFichaje.setStartAt(OffsetDateTime.now());
            nuevoFichaje.setCreatedAt(OffsetDateTime.now());

            nuevoFichaje.setStartLat(requestDto.getLat());
            nuevoFichaje.setStartLng(requestDto.getLng());
            nuevoFichaje.setStartAccuracyM(requestDto.getAccuracyMeters());
            nuevoFichaje.setStartIp(ip);
            nuevoFichaje.setStartUserAgent(userAgent);
            nuevoFichaje.setStartGeoip(ipResult.geoIpMap());

            nuevoFichaje.setFlags(flags);
            nuevoFichaje.setStatus(Status.OPEN);

            fichajeGuardado = timeEntryRepository.save(nuevoFichaje);
        }

        TimeEntryResponseDto response = new TimeEntryResponseDto();
        response.setId(fichajeGuardado.getId());
        response.setStartAt(fichajeGuardado.getStartAt());
        response.setEndAt(fichajeGuardado.getEndAt());
        response.setStatus(fichajeGuardado.getStatus().name());

        return response;
    }

    @Transactional(readOnly = true)
    public ResumenDiarioResponseDto getResumenDiario() {
        UsuarioYCompaniaInfo info = extraerUsuarioYCompania();
        User user = info.getUser();
        Profile profile = info.getProfile();

        LocalDate hoy = LocalDate.now();
        DayOfWeek diaSemana = hoy.getDayOfWeek();

        Optional<TimeEntry> fichajeActual = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndStatus(user.getId(), Status.OPEN);

        Long minutosAcumulados = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDate(user.getId(), hoy);

        Optional<WorkSchedule> horario = workScheduleRepository
                .findByEmployee_UserIdAndDayOfWeek(profile.getUserId(), diaSemana);

        Duration objetivoMin;
        if (horario.isPresent()) {
            LocalTime start = horario.get().getStartTime();
            LocalTime end = horario.get().getEndTime();

            objetivoMin = Duration.between(start, end);

            if (objetivoMin.isNegative()) {
                objetivoMin = objetivoMin.plusDays(1);
            }
        } else {
            objetivoMin = Duration.ofMinutes(0);
        }

        List<TimeEntry> ultimosTurnos = timeEntryRepository.
                findTop5ByEmployee_UserIdOrderByStartAtDesc(user.getId());

        List<UltimosFichajesResponseDto> eventosSueltos = new ArrayList<>();

        for (TimeEntry turno : ultimosTurnos) {
            eventosSueltos.add(new UltimosFichajesResponseDto(
                    turno.getId(), "Entrada", turno.getStartAt()));

            if (turno.getEndAt() != null) {
                eventosSueltos.add(new UltimosFichajesResponseDto(
                        turno.getId(), "Salida", turno.getEndAt()));
            }
        }

        List<UltimosFichajesResponseDto> ultimos5Fichajes = eventosSueltos.stream()
                .sorted((e1, e2)
                        -> e2.getFecha().compareTo(e1.getFecha()))
                .limit(5)
                .toList();

        ResumenDiarioResponseDto dto = new ResumenDiarioResponseDto();
        dto.setHoraEntrada(fichajeActual.map(TimeEntry::getStartAt).orElse(null));
        dto.setMinutosAcumulados(minutosAcumulados);
        dto.setMinutosObjetivo(objetivoMin.toMinutes());
        dto.setUltimosFichajes(ultimos5Fichajes);

        return dto;
    }
}
