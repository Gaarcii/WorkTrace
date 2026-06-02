package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.in;

import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.service.auth.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador REST del cierre diario.
 * <p>
 * Expone los endpoints utilizados para ejecutar el proceso de cierre de una
 * fecha concreta y para verificar la integridad criptográfica de las
 * marcaciones asociadas a la compañía autenticada.
 * <p>
 * Este controlador actúa como adaptador de entrada dentro de Clean Architecture:
 * recibe la petición HTTP, valida el contexto de seguridad a través de Spring
 * Security y delega la lógica de negocio en los casos de uso del dominio.
 */
@RestController
@RequestMapping("/api/daily-closures")
@RequiredArgsConstructor
@Tag(name = "Cierre diario", description = "Operaciones para ejecutar y verificar el cierre diario")
public class DailyClosureController {

    private final RunDailyClosureUseCase runDailyClosureUseCase;
    private final VerifyIntegrityUseCase verifyIntegrityUseCase;
    private final UserService userService;

    /**
     * Ejecuta el cierre diario para la fecha indicada.
     * <p>
     * Regla de negocio:
     * - Solo un usuario con rol ADMIN puede disparar el proceso de cierre.
     * - La fecha recibida identifica el día laboral sobre el que se calculan y
     * consolidan los datos del cierre.
     * - La ejecución se delega al caso de uso de aplicación, manteniendo el
     * controlador libre de lógica de negocio.
     *
     * @param date Fecha del cierre diario que se desea procesar.
     * @return Respuesta HTTP 200 (OK) sin cuerpo cuando la ejecución se ha
     * lanzado correctamente.
     */
    @Operation(summary = "Ejecutar cierre diario", description = "Dispara el proceso de cierre para una fecha concreta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cierre diario ejecutado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para ejecutar el cierre")
    })
    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> runClosure(@Parameter(description = "Fecha del cierre diario a procesar", example = "2026-06-02")
                                    @RequestParam LocalDate date) {
        runDailyClosureUseCase.execute(date);
        return ResponseEntity.ok().build();
    }

    /**
     * Verifica la integridad de los datos de cierre diario para la fecha
     * indicada y la compañía autenticada.
     * <p>
     * Regla de negocio:
     * - El endpoint está disponible para usuarios con rol ADMIN o INSPECTOR.
     * - La verificación siempre se realiza sobre la compañía del usuario
     * autenticado, garantizando el aislamiento multi-tenant.
     * - La fecha se usa para recuperar el estado de integridad correspondiente
     * al día consultado.
     *
     * @param date Fecha del cierre diario cuya integridad se desea comprobar.
     * @return {@link IntegrityResult} con el estado de verificación, el hash
     * calculado y la información necesaria para auditar el resultado.
     */
    @Operation(summary = "Verificar integridad del cierre diario", description = "Consulta el resultado de integridad para la compañía autenticada y una fecha concreta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Integridad calculada correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para consultar la integridad")
    })
    @GetMapping("/integrity")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
    ResponseEntity<IntegrityResult> verifyIntegrity(@Parameter(description = "Fecha del cierre diario a verificar", example = "2026-06-02")
                                                    @RequestParam LocalDate date) {
        UUID companyId = userService.getAuthenticatedUserAndCompanyInfo().getCompany().getId();
        IntegrityResult result = verifyIntegrityUseCase.execute(companyId,date);
        return ResponseEntity.ok(result);
    }
}
