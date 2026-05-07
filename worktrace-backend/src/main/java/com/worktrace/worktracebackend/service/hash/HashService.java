package com.worktrace.worktracebackend.service.hash;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Servicio dedicado a la generación de hashes criptográficos.
 * Su propósito es proporcionar una manera centralizada y estandarizada de calcular
 * "huellas digitales" de datos, lo cual es fundamental para garantizar la integridad
 * y la inalterabilidad de la información en diferentes partes del sistema, como en los
 * informes PDF y los cierres diarios.
 */
@Service
public class HashService {

    /**
     * Calcula el hash SHA-256 de una cadena de datos y lo devuelve en formato hexadecimal.
     * El algoritmo SHA-256 es un estándar de la industria que asegura que cualquier cambio,
     * por mínimo que sea, en los datos de entrada producirá un hash completamente diferente,
     * haciendo evidente cualquier manipulación.
     *
     * @param rawData La cadena de datos de entrada que se va a hashear.
     * @return Una cadena de texto que representa el hash en formato hexadecimal.
     * @throws IllegalStateException Si el algoritmo SHA-256 no está disponible en el entorno de Java,
     *                               lo cual es extremadamente improbable en sistemas modernos.
     */
    public String sha256Hex(String rawData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error fatal: Algoritmo SHA-256 no disponible en el sistema", e);
        }
    }
}
