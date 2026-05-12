package com.worktrace.worktracebackend.service.hash;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HashServiceTest {

    @InjectMocks
    private HashService hashService;

    @Test
    void testSha256HexReturnsCorrectAndConsistentHash() {
        String inputData = "WorkTrace";
        String expectedHash = "6a8f75c8d31cf8b3c67f626e9c55b483fa37a9d53a2fc5da7de96ded418874cc";

        String actualHash = hashService.sha256Hex(inputData);

        assertAll("Verificaciones para un hash SHA-256 válido",
                () -> assertNotNull(actualHash, "El hash generado no debe ser nulo"),
                () -> assertEquals(64, actualHash.length(), "La longitud del hash SHA-256 debe ser 64 caracteres"),
                () -> assertEquals(expectedHash, actualHash, "El hash para una entrada conocida debe ser el correcto")
        );
    }

    @Test
    void testSha256HexWithEmptyString() {
        String inputData = "";
        String expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String actualHash = hashService.sha256Hex(inputData);

        assertEquals(expectedHash, actualHash, "El hash para una cadena vacía debe coincidir con el estándar conocido");
    }

    @Test
    void testSha256HexIsDeterministic() {
        String inputData = "datos_repetidos_123";

        String firstHash = hashService.sha256Hex(inputData);
        String secondHash = hashService.sha256Hex(inputData);

        assertEquals(firstHash, secondHash, "El algoritmo de hash debe ser determinista, produciendo el mismo resultado para la misma entrada");
    }

    @Test
    void testSha256HexHandlesDifferentInputs() {
        String input1 = "datos de entrada 1";
        String input2 = "datos de entrada 2";

        String hash1 = hashService.sha256Hex(input1);
        String hash2 = hashService.sha256Hex(input2);

        assertNotEquals(hash1, hash2, "Entradas diferentes deben producir hashes diferentes");
    }
}
