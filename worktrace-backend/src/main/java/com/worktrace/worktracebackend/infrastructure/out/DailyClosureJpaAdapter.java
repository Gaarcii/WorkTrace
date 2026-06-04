package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.DailyClosure;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.DailyClosureRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link DailyClosurePort} sobre JPA.
 * <p>
 * Persiste y consulta los registros de cierre diario, traduciendo entre el
 * modelo de dominio {@link DailyClosureRecord} y la entidad JPA
 * {@link DailyClosure}. Da soporte al encadenamiento de hashes entre cierres
 * consecutivos, base de la protección de integridad.
 */
@Repository
@Transactional(readOnly = true)
public class DailyClosureJpaAdapter implements DailyClosurePort {

    private final DailyClosureRepository dailyClosureRepository;
    private final CompanyRepository companyRepository;

    public DailyClosureJpaAdapter(DailyClosureRepository dailyClosureRepository, CompanyRepository companyRepository) {
        this.dailyClosureRepository = dailyClosureRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Comprueba si ya existe un cierre para la empresa y la fecha indicadas,
     * evitando cierres duplicados.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha del cierre a comprobar.
     * @return {@code true} si ya existe un cierre para esa empresa y fecha.
     */
    @Override
    public boolean existsForDate(UUID companyId, LocalDate date) {
        return dailyClosureRepository.existsByCompanyIdAndWorkDate(companyId, date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera el hash del cierre inmediatamente anterior a la fecha dada, para
     * encadenarlo con el nuevo cierre.
     *
     * @param companyId  Identificador de la empresa.
     * @param beforeDate Fecha límite; se busca el último cierre anterior a ella.
     * @return El hash del cierre previo, o vacío si no existe ninguno.
     */
    @Override
    public Optional<String> findPreviousHash(UUID companyId, LocalDate beforeDate) {
        return dailyClosureRepository
                .findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(companyId, beforeDate)
                .map(DailyClosure::getDayHash);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Persiste un nuevo registro de cierre diario, resolviendo la referencia a
     * la empresa mediante un proxy para no cargar la entidad completa.
     *
     * @param record Datos del cierre a guardar.
     */
    @Override
    @Transactional
    public void save(DailyClosureRecord record) {

        Company companyProxy = companyRepository.getReferenceById(record.companyId());

        DailyClosure dailyClosure = new DailyClosure();
        dailyClosure.setCompany(companyProxy);
        dailyClosure.setWorkDate(record.workDate());
        dailyClosure.setDayHash(record.dayHash());
        dailyClosure.setPrevDayHash(record.prevDayHash());
        dailyClosure.setRecordsCount(record.recordsCount());
        dailyClosure.setComputedAt(record.computedAt());

        dailyClosureRepository.save(dailyClosure);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca el cierre de una empresa para una fecha concreta y lo traduce al
     * modelo de dominio.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha del cierre a buscar.
     * @return El cierre encontrado como {@link DailyClosureRecord}, o vacío si
     * no existe.
     */
    @Override
    public Optional<DailyClosureRecord> findByDate(UUID companyId, LocalDate date) {
        return dailyClosureRepository.findByCompanyIdAndWorkDate(companyId, date)
                .map(this::toRecord);
    }

    /**
     * Convierte la entidad JPA {@link DailyClosure} en el modelo de dominio
     * {@link DailyClosureRecord}.
     *
     * @param jpa Entidad de cierre persistida.
     * @return El registro de cierre en el modelo de dominio.
     */
    private DailyClosureRecord toRecord(DailyClosure jpa) {
        return new DailyClosureRecord(
                jpa.getCompany().getId(),
                jpa.getWorkDate(),
                jpa.getDayHash(),
                jpa.getPrevDayHash(),
                jpa.getRecordsCount(),
                jpa.getComputedAt()
        );
    }
}