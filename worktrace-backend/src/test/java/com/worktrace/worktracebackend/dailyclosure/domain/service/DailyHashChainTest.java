package com.worktrace.worktracebackend.dailyclosure.domain.service;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DailyHashChainTest {

    private DailyHashChain hashChain;

    @BeforeEach
    void setUp() {
        hashChain = new DailyHashChain(input -> "hash_fijo");
    }

    @Test
    void compute_sinSnapshots_devuelveHashConPrefixoVersion() {
        String result = hashChain.compute(List.of(), "GENESIS");

        assertEquals("V2:hash_fijo", result);
    }

    @Test
    void compute_conSnapshots_devuelveHashConPrefixoVersion() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();

        String result = hashChain.compute(List.of(snapshot), "GENESIS");

        assertEquals("V2:hash_fijo", result);
    }

    @Test
    void compute_mismosDatos_produceHashDeterminista() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();
        DailyHashChain chainReal = new DailyHashChain(new com.worktrace.worktracebackend.service.hash.HashService()::sha256Hex);

        String resultado1 = chainReal.compute(List.of(snapshot), "GENESIS");
        String resultado2 = chainReal.compute(List.of(snapshot), "GENESIS");

        assertEquals(resultado1, resultado2);
    }

    @Test
    void compute_hashPrevioDistinto_produceHashDistinto() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();
        DailyHashChain chainReal = new DailyHashChain(new com.worktrace.worktracebackend.service.hash.HashService()::sha256Hex);

        String conGenesisHash = chainReal.compute(List.of(snapshot), "GENESIS_HASH_A");
        String conOtroHash = chainReal.compute(List.of(snapshot), "GENESIS_HASH_B");

        assertNotEquals(conGenesisHash, conOtroHash);
    }

    @Test
    void compute_listasVacias_incluyeNoActivity() {
        DailyHashChain chainCapturadora = new DailyHashChain(input -> {
            assertTrue(input.contains("NO_ACTIVITY"), "El input debe contener NO_ACTIVITY cuando no hay snapshots");
            return "ok";
        });

        chainCapturadora.compute(List.of(), "prevHash");
    }

    private TimeEntrySnapshot snapshotDeEjemplo() {
        return new TimeEntrySnapshot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(8),
                new BigDecimal("40.416775"),
                new BigDecimal("-3.703790"),
                new BigDecimal("40.416775"),
                new BigDecimal("-3.703790"),
                10,
                10,
                "192.168.1.1",
                "192.168.1.1",
                "Mozilla/5.0",
                "Mozilla/5.0",
                null,
                null,
                null,
                TimeEntryStatus.CLOSED,
                null,
                null,
                null,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                null,
                null,
                UUID.randomUUID()
        );
    }
}
