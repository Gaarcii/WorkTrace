package com.worktrace.worktracebackend.dailyclosure.domain.service;

import com.worktrace.worktracebackend.dailyclosure.domain.model.HashResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DailyHashChainTest {

    private DailyHashChain hashChain;

    @BeforeEach
    void setUp() {
        hashChain = new DailyHashChain();
    }

    @Test
    void compute_sinSnapshots_devuelveHashConPrefixoVersion() {
        HashResult result = hashChain.compute(Stream.of(), "GENESIS");

        assertTrue(result.hash().startsWith("V2:"));
        assertEquals(0, result.recordCount());
    }

    @Test
    void compute_conSnapshots_devuelveHashConPrefixoVersion() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();

        HashResult result = hashChain.compute(Stream.of(snapshot), "GENESIS");

        assertTrue(result.hash().startsWith("V2:"));
        assertEquals(1, result.recordCount());
    }

    @Test
    void compute_mismosDatos_produceHashDeterminista() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();

        HashResult resultado1 = hashChain.compute(Stream.of(snapshot), "GENESIS");
        HashResult resultado2 = hashChain.compute(Stream.of(snapshot), "GENESIS");

        assertEquals(resultado1, resultado2);
    }

    @Test
    void compute_hashPrevioDistinto_produceHashDistinto() {
        TimeEntrySnapshot snapshot = snapshotDeEjemplo();

        HashResult conGenesisHash = hashChain.compute(Stream.of(snapshot), "GENESIS_HASH_A");
        HashResult conOtroHash = hashChain.compute(Stream.of(snapshot), "GENESIS_HASH_B");

        assertNotEquals(conGenesisHash.hash(), conOtroHash.hash());
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