package com.worktrace.worktracebackend.shared.model;

import java.util.List;

/**
 * Página de resultados genérica e independiente del framework.
 * <p>
 * Modelo compartido que transporta una porción paginada de resultados junto con
 * sus metadatos, sin acoplar el dominio a los tipos de paginación de Spring Data
 * ({@code Page}/{@code Pageable}). La traducción a/desde esos tipos se hace en la
 * capa de infraestructura.
 *
 * @param content       Elementos de la página actual.
 * @param page          Índice de la página (base 0).
 * @param size          Tamaño de página solicitado.
 * @param totalElements Número total de elementos en todas las páginas.
 * @param totalPages    Número total de páginas.
 * @param <T>           Tipo de los elementos contenidos.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
