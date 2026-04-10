package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.auditTimeEntries.AuditRecordDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.timeEntry.*;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.WorkScheduleRepository;
import com.worktrace.worktracebackend.service.archivos.ExcelGeneratorService;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import com.worktrace.worktracebackend.service.incidence.IncidenceService;
import com.worktrace.worktracebackend.service.ip.IpDetectionService;
import com.worktrace.worktracebackend.service.archivos.AdminPdfGeneratorService;
import com.worktrace.worktracebackend.service.archivos.EmployeePdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public TimeEntryResponseDto procesarFichaje(TimeEntryRequestDto requestDto, String ip, String userAgent) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        User user = info.getUser();
        Company company = info.getCompany();
        Profile profile = info.getProfile();

        Optional<TimeEntry> turnoAbiertoOpt = timeEntryRepository.
                findByEmployee_UserIdAndEndAtIsNullAndEstadoFichaje(profile.getUserId(), EstadoFichaje.OPEN);

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

            turnoAbierto.setEstadoFichaje(EstadoFichaje.CLOSED);
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
            nuevoFichaje.setEstadoFichaje(EstadoFichaje.OPEN);

            fichajeGuardado = timeEntryRepository.save(nuevoFichaje);
        }

        TimeEntryResponseDto response = new TimeEntryResponseDto();
        response.setId(fichajeGuardado.getId());
        response.setStartAt(fichajeGuardado.getStartAt());
        response.setEndAt(fichajeGuardado.getEndAt());
        response.setStatus(fichajeGuardado.getEstadoFichaje().name());

        return response;
    }

    @Transactional(readOnly = true)
    public ResumenDiarioResponseDto getResumenDiario() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

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
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        ResumenDiarioResponseDto resumenDiario = calcularDatosDelDia(info.getUser(), info.getProfile(), fecha);

        List<TimeEntry> fichajesDia = timeEntryRepository.
                findTimeEntriesByEmployee_UserIdAndWorkDate(info.getUser().getId(), fecha);

        LocalDate fechaInicio = fecha.with(DayOfWeek.MONDAY);
        LocalDate fechaFin = fecha.with(DayOfWeek.SUNDAY);

        Long minutosTrabajadosSemanales = timeEntryRepository.
                getWorkedMinutesByEmployeeAndDateRange(info.getProfile().getUserId(), fechaInicio, fechaFin);

        Long minutosSemanales = info.getProfile().getWeeklyHours() != null
                ? info.getProfile().getWeeklyHours().multiply(BigDecimal.valueOf(60)).longValue()
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
                findByEmployee_UserIdAndEndAtIsNullAndEstadoFichaje(user.getId(), EstadoFichaje.OPEN);

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
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
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

        List<WorkerIncidenceResponseDto> incidencias = incidenceService
                .getIncidenciaPorFechas(fechaInicio, fechaFin);

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
        responseDto.setIncidenciasList(incidencias);

        return responseDto;
    }

    @Transactional(readOnly = true)
    public byte[] exportarHistorialPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        List<TimeEntry> fichajes = timeEntryRepository
                .findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
                        info.getProfile().getUserId(),
                        fechaInicio,
                        fechaFin
                );

        return employeePdfGeneratorService.generarPDFFichajes(
                info.getProfile(),
                info.getUser(),
                fichajes,
                fechaInicio,
                fechaFin
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveWorkerDto> getActiveWorkers() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> timeEntryList = timeEntryRepository
                .getAllByCompany_IdAndEndAtIsNull(company.getId());

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return timeEntryList.stream()
                .map(timeEntry -> {
                    Profile employee = timeEntry.getEmployee();
                    String puesto = null;
                    if (employee.getPosition() != null) {
                        puesto = employee.getPosition().getTitle();
                    }
                    Optional<WorkSchedule> horarioOpt = workScheduleRepository
                            .findByEmployee_UserIdAndDayOfWeek(employee.getUserId(), today);
                    Long puntualidad = null;
                    if (horarioOpt.isPresent() && timeEntry.getStartAt() != null) {
                        OffsetDateTime horaEntrada = timeEntry.getStartAt();
                        LocalTime horaHorario = horarioOpt.get().getStartTime();
                        puntualidad = Duration.between(horaHorario, horaEntrada).toMinutes();
                    }
                    return new ActiveWorkerDto(
                            employee.getUserId(),
                            employee.getFullName(),
                            puesto,
                            employee.getAvatarUrl(),
                            timeEntry.getStartAt(),
                            puntualidad
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getFichajesCountToday() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        return timeEntryRepository.countAllByCompany_IdAndWorkDateBetween(
                company.getId(), LocalDate.now(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public HorasTrabajadasHoyResponseDto getHorasTotalesHoy() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        Long minutosTotales = timeEntryRepository
                .getWorkedMinutesByCompanyAndDate(company.getId(), LocalDate.now());

        if (minutosTotales == null) {
            minutosTotales = 0L;
        }

        return new HorasTrabajadasHoyResponseDto(minutosTotales);
    }

    @Transactional(readOnly = true)
    public List<DailyFichajeCountDto> getWeeklyChartData(LocalDate startDate, LocalDate endDate) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<DailyFichajeCountProjection> fichajesAgrupados = timeEntryRepository
                .getFichajesCountByCompanyAndDateRange(
                        company.getId(), startDate, endDate);

        Map<LocalDate, Long> conteoPorFecha = new HashMap<>();
        for (DailyFichajeCountProjection proy : fichajesAgrupados) {
            conteoPorFecha.put(proy.getFecha(), proy.getNumFichajes());
        }

        List<DailyFichajeCountDto> resultado = new ArrayList<>();
        LocalDate fecha = startDate;
        while (!fecha.isAfter(endDate)) {
            Long numFichajes = conteoPorFecha.getOrDefault(fecha, 0L);
            resultado.add(new DailyFichajeCountDto(fecha.toString(), numFichajes));
            fecha = fecha.plusDays(1);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public LocalDate primerFichaje() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        LocalDate primeraFecha = timeEntryRepository.findFirstWorkDateByEmployee
                (info.getProfile().getUserId());
        return primeraFecha != null ? primeraFecha : LocalDate.now();
    }

    @Transactional
    public void editarFichaje(UUID id, EditTimeEntryRequestDto dto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        TimeEntry fichaje = comprobarFichaje(id, company);

        fichaje.setStartAt(dto.getEntrada());
        fichaje.setWorkDate(dto.getEntrada().toLocalDate());

        if (dto.getSalida() != null) {
            fichaje.setEndAt(dto.getSalida());
            fichaje.setEstadoFichaje(EstadoFichaje.CLOSED);
        } else {
            fichaje.setEndAt(null);
            fichaje.setEstadoFichaje(EstadoFichaje.OPEN);
        }

        fichaje.setModificationReason(dto.getJustificacion());
        fichaje.setUpdatedAt(OffsetDateTime.now());
    }

    @Transactional
    public void anularFichaje(UUID id, AnularTimeEntryRequestDto dto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();
        TimeEntry fichaje = comprobarFichaje(id, company);

        fichaje.setDeletedAt(OffsetDateTime.now());
        fichaje.setDeletedBy(info.getUser());
        fichaje.setDeleteReason(dto.getJustificacion());
        fichaje.setUpdatedAt(OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<FichajeTablaResponseDto> getFichajesPaginadosPorEmpleado
            (UUID employeeId, Pageable pageable) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();

        User trabajador = userService.getUserById(employeeId);
        if (!trabajador.getCompany().getId().equals(info.getCompany().getId())) {
            throw new IllegalStateException("El trabajador no pertenece a tu empresa");
        }

        Page<TimeEntry> page = timeEntryRepository
                .findByEmployee_UserIdAndDeletedAtIsNullOrderByWorkDateDesc(employeeId, pageable);

        return page.map(f -> {
            Long horasTrabajadas = null;

            if (f.getStartAt() != null && f.getEndAt() != null) {
                Duration duracion = Duration.between(f.getStartAt(), f.getEndAt());
                horasTrabajadas = duracion.toMinutes();
            }

            return new FichajeTablaResponseDto(
                    f.getId(),
                    f.getWorkDate(),
                    f.getStartAt(),
                    f.getEndAt(),
                    f.getStartLat(),
                    f.getStartLng(),
                    f.getEndLat(),
                    f.getEndLng(),
                    horasTrabajadas
            );
        });
    }

    @Transactional(readOnly = true)
    public byte[] exportarInformeEmpresaPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> fichajes = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), fechaInicio, fechaFin);

        List<AuditRecordDto> auditoria = List.of();

        return adminPdfGeneratorService.generarPDFFichajesEmpresa(company, fichajes, fechaInicio, fechaFin, auditoria);
    }

    @Transactional(readOnly = true)
    public byte[] exportarInformeEmpresaExcel(LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        Company company = info.getCompany();

        List<TimeEntry> fichajes = timeEntryRepository
                .findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(company.getId(), fechaInicio, fechaFin);

        List<AuditRecordDto> auditoria = List.of();

        return excelGeneratorService.generarExcelFichajesEmpresa(fichajes, auditoria, fechaInicio, fechaFin);
    }

    private TimeEntry comprobarFichaje(UUID id, Company company) {
        Optional<TimeEntry> fichaje = timeEntryRepository.findById(id);
        if (fichaje.isEmpty()) {
            throw new NotFoundException("No se encontró el fichajeOp");
        }
        if (!fichaje.get().getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El fichajeOp no pertenece a tu empresa");
        }
        return fichaje.get();
    }

}
