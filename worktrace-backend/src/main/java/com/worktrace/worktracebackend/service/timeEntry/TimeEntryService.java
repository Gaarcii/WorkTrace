package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidentRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final IncidentRepository incidentRepository;

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

        ResumenDiarioResponseDto dto = calcularDatosDelDia(info.getUser(), info.getProfile(), LocalDate.now());

        List<TimeEntry> ultimosTurnos = timeEntryRepository.findTop5ByEmployee_UserIdOrderByStartAtDesc(info.getUser().getId());
        List<UltimosFichajesResponseDto> ultimos5Fichajes = mapTimeEntriesToEventos(ultimosTurnos).stream()
                .sorted((e1, e2) -> e2.getFecha().compareTo(e1.getFecha()))
                .limit(5)
                .toList();
        dto.setUltimosFichajes(ultimos5Fichajes);

        return dto;
    }

    @Transactional(readOnly = true)
    public HistorialResponseDto getHistorial(LocalDate fecha) {
        UsuarioYCompaniaInfo info = extraerUsuarioYCompania();
        ResumenDiarioResponseDto resumenDiario = calcularDatosDelDia(info.getUser(), info.getProfile(), fecha);

        List<TimeEntry> fichajesDia = timeEntryRepository.
                findTimeEntriesByEmployee_UserIdAndWorkDate(info.getUser().getId(), fecha);

        LocalDate fechaInicio = fecha.with(DayOfWeek.MONDAY);
        LocalDate fechaFin = fecha.with(DayOfWeek.SUNDAY);

        Long minutosTrabajadosSemanales = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDateRange(info.getProfile().getUserId(), fechaInicio, fechaFin);

        Long minutosSemanales = info.getProfile().getWeeklyHours() != null
                ? info.getProfile().getWeeklyHours().multiply(java.math.BigDecimal.valueOf(60)).longValue()
                : 0L;

        List<UltimosFichajesResponseDto> registrosDia = mapTimeEntriesToEventos(fichajesDia).stream()
                .sorted((e1, e2) -> e2.getFecha().compareTo(e1.getFecha()))
                .toList();
        HistorialResponseDto historialResponseDto = new HistorialResponseDto();
        historialResponseDto.setMinutosObjetivoDia(resumenDiario.getMinutosObjetivo());
        historialResponseDto.setMinutosTrabajadosDia(resumenDiario.getMinutosAcumulados());
        historialResponseDto.setRegistrosDia(registrosDia);
        historialResponseDto.setMinutosObjetivoSemana(minutosSemanales);
        historialResponseDto.setMinutosTrabajadosSemana(minutosTrabajadosSemanales);
        return historialResponseDto;
    }


    private List<UltimosFichajesResponseDto> mapTimeEntriesToEventos(List<TimeEntry> turnos) {
        List<UltimosFichajesResponseDto> eventos = new ArrayList<>();
        for (TimeEntry turno : turnos) {
            eventos.add(new UltimosFichajesResponseDto(
                    turno.getId(), "Entrada", turno.getStartAt()));
            if (turno.getEndAt() != null) {
                eventos.add(new UltimosFichajesResponseDto(
                        turno.getId(), "Salida", turno.getEndAt()));
            }
        }
        return eventos;
    }

    private ResumenDiarioResponseDto calcularDatosDelDia(User user, Profile profile, LocalDate fecha) {
        DayOfWeek diaSemana = fecha.getDayOfWeek();

        Optional<TimeEntry> fichajeActual = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndStatus(user.getId(), Status.OPEN);

        Long minutosAcumulados = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDate(user.getId(), fecha);

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

        ResumenDiarioResponseDto dtoInfoDia = new ResumenDiarioResponseDto();
        dtoInfoDia.setHoraEntrada(fichajeActual.map(TimeEntry::getStartAt).orElse(null));
        dtoInfoDia.setMinutosAcumulados(minutosAcumulados);
        dtoInfoDia.setMinutosObjetivo(objetivoMin.toMinutes());

        return dtoInfoDia;
    }

    public EstadisticasResponseDto getEstadisticas(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = extraerUsuarioYCompania();
        EstadisticasResponseDto responseDto = new EstadisticasResponseDto();

        List<WorkSchedule> horariosList = workScheduleRepository
                .findByEmployee_UserId(info.getProfile().getUserId());

        Map<DayOfWeek, Long> objetivoPorDia = new EnumMap<>(DayOfWeek.class);
        for (WorkSchedule h : horariosList) {
            Duration duracion = Duration.between(h.getStartTime(), h.getEndTime());
            if (duracion.isNegative()) {
                duracion = duracion.plusDays(1);
            }
            objetivoPorDia.put(DayOfWeek.valueOf(
                    h.getDayOfWeek().toString()), duracion.toMinutes());
        }

        List<EstadisticaDiariaProjection> registrosAgrupados = timeEntryRepository
                .getEstadisticasDiariasAgrupadas(info.getProfile().getUserId(), fechaInicio, fechaFin);

        Map<LocalDate, Long> mapaTrabajadoPorDia = registrosAgrupados.stream()
                .collect(Collectors.toMap(
                        EstadisticaDiariaProjection::getFecha,
                        EstadisticaDiariaProjection::getMinutosTrabajados
                ));

        long totalTrabajados = 0L;
        long balanceTotal = 0L;
        int jornadasIncompletas = 0;

        LocalDate diaActual = fechaInicio;
        List<EstadisticaDiariaDto> resumenesDiarios = new ArrayList<>();

        while (!diaActual.isAfter(fechaFin)) {
            long minutosPrevistos = objetivoPorDia.getOrDefault(
                    diaActual.getDayOfWeek(), 0L);
            long minutosTrabajados = mapaTrabajadoPorDia.getOrDefault(
                    diaActual, 0L);

            EstadisticaDiariaDto diariaDto = new EstadisticaDiariaDto();
            diariaDto.setFecha(diaActual);
            diariaDto.setMinutosPrevistos(minutosPrevistos);
            diariaDto.setMinutosTrabajados(minutosTrabajados);

            totalTrabajados += minutosTrabajados;
            balanceTotal += (minutosTrabajados - minutosPrevistos);

            if (minutosPrevistos > 0 && minutosTrabajados < minutosPrevistos) {
                jornadasIncompletas++;
            }

            resumenesDiarios.add(diariaDto);

            diaActual = diaActual.plusDays(1);
        }

        int totalIncidencias = incidentRepository.countIncidentsByUsuarioYFechas(
                info.getUser().getId(),
                fechaInicio,
                fechaFin
        );

        responseDto.setMinutosTrabajadosTotal(totalTrabajados);
        responseDto.setBalanceMinutos(balanceTotal);
        responseDto.setJornadasIncompletas(jornadasIncompletas);
        responseDto.setIncidencias(totalIncidencias);
        responseDto.setResumenDiario(resumenesDiarios);

        return responseDto;
    }
}
