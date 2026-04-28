package com.worktrace.worktracebackend.service.dailyClosure;

import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.DailyClosure;
import com.worktrace.worktracebackend.model.EstadoFichaje;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.DailyClosureRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.hash.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyClosureService {

    private static final String HASH_VERSION_V2 = "V2";
    private static final String HASH_SEPARATOR = ":";

    private final CompanyRepository companyRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final DailyClosureRepository dailyClosureRepository;
    private final HashService hashService;
    private final UserService userService;
    private final AuditTimeEntryRepository auditTimeEntryRepository;


   @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void runDailyClosure() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            try {
                procesarCierreIndividual(company, targetDate);
            } catch (Exception e) {
                log.error("Fallo en cierre de empresa {}: {}", company.getCompanyName(), e.getMessage());
            }
        }
   }

    private void procesarCierreIndividual(Company company, LocalDate targetDate) {
        if (dailyClosureRepository.existsByCompanyIdAndWorkDate(company.getId(), targetDate)) {
            throw new RuntimeException("El cierre para la fecha " + targetDate + " ya está realizado.");
        }

        long openShifts = timeEntryRepository.countByCompanyIdAndWorkDateAndEstadoFichaje(
                company.getId(), targetDate, EstadoFichaje.OPEN);

        if (openShifts > 0) {
            throw new RuntimeException("Hay " + openShifts + " turnos abiertos.");
        }

        String previousHash = dailyClosureRepository.findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(
                        company.getId(), targetDate)
                .map(DailyClosure::getDayHash)
                .orElse("GENESIS_HASH_0000000000000000000000000000");

        List<TimeEntry> entries = timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(
                company.getId(), targetDate);

        String newHash = generarHashDelDia(entries, previousHash);

        DailyClosure closure = new DailyClosure();
        closure.setWorkDate(targetDate);
        closure.setCompany(company);
        closure.setRecordsCount(entries.size());
        closure.setDayHash(newHash);
        closure.setPrevDayHash(previousHash);
        closure.setComputedAt(OffsetDateTime.now());

        dailyClosureRepository.save(closure);
    }

    public String verificarIntegridad(LocalDate date) {
        UUID companyId = userService.extraerUsuarioYCompania().getCompany().getId();

        DailyClosure closure = dailyClosureRepository.findByCompanyIdAndWorkDate(companyId, date)
                .orElseThrow(() -> new RuntimeException("No hay cierre para esta fecha"));

        List<TimeEntry> entries = timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(companyId, date);

        String currentHash = generarHashDelDia(entries, closure.getPrevDayHash());
        String storedHash = closure.getDayHash();

        if (!storedHash.startsWith(HASH_VERSION_V2 + HASH_SEPARATOR)) {
            currentHash = generarHashDelDia(entries, closure.getPrevDayHash());
        }

        if (currentHash.equals(storedHash)) {
            return "VALID";
        }

        boolean modificadoConJustificacion = auditTimeEntryRepository.existsEditsAfterClosure(
                companyId, date, closure.getComputedAt());

        return modificadoConJustificacion ? "MODIFIED" : "CORRUPTED";
    }

    private String generarHashDelDia(List<TimeEntry> entries, String prevHash) {
        StringBuilder rawData = new StringBuilder();
        if (entries.isEmpty()) {
            rawData.append("NO_ACTIVITY");
        } else {
            for (TimeEntry entry : entries) {
                rawData.append(entry.getId())
                        .append(entry.getEmployee().getUserId())
                        .append(entry.getWorkDate())
                        .append(entry.getStartAt() != null ? entry.getStartAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(entry.getEndAt() != null ? entry.getEndAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(entry.getStartLat())
                        .append(entry.getStartLng())
                        .append(entry.getEndLat() != null ? entry.getEndLat() : "NULL")
                        .append(entry.getEndLng() != null ? entry.getEndLng() : "NULL")
                        .append(entry.getStartAccuracyM())
                        .append(entry.getEndAccuracyM() != null ? entry.getEndAccuracyM() : "NULL")
                        .append(entry.getStartIp())
                        .append(entry.getEndIp() != null ? entry.getEndIp() : "NULL")
                        .append(entry.getStartUserAgent())
                        .append(entry.getEndUserAgent() != null ? entry.getEndUserAgent() : "NULL")
                        .append(entry.getStartGeoip())
                        .append(entry.getEndGeoip() != null ? entry.getEndGeoip() : "NULL")
                        .append(entry.getFlags() != null ? entry.getFlags() : "NULL")
                        .append(entry.getEstadoFichaje())
                        .append(entry.getDeletedAt() != null ? entry.getDeletedAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(entry.getDeletedBy() != null ? entry.getDeletedBy().getId() : "NULL").append(entry.getDeleteReason() != null ? entry.getDeleteReason() : "NULL")
                        .append(entry.getCreatedAt() != null ? entry.getCreatedAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(entry.getCreatedBy() != null ? entry.getCreatedBy().getId() : "NULL").append(entry.getUpdatedAt() != null ? entry.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(entry.getModificationReason() != null ? entry.getModificationReason() : "NULL")
                        .append(entry.getCompany() != null ? entry.getCompany().getId() : "NULL");
            }
        }

        String stringGigante = prevHash + rawData;
        return hashService.sha256Hex(stringGigante);
    }
}