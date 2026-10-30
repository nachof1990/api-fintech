package com.diplomatura.apifintech.controller;

import com.diplomatura.apifintech.dto.CrearCuentaRequest;
import com.diplomatura.apifintech.dto.CuentaResponse;
import com.diplomatura.apifintech.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.util.List;
import com.diplomatura.apifintech.dto.ActualizarCuentaRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Recibe solicitudes HTTP de cuentas y delega su resolución al servicio.
 */
@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "Creación y consulta de cuentas bancarias")
public class CuentaController {

    private final CuentaService cuentaService;

    /**
     * @param cuentaService servicio encargado de gestionar las cuentas
     */
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    /**
     * @return lista de cuentas; estará vacía si no hay registros
     */
    @Operation(
            summary = "Listar cuentas",
            description = "Devuelve cajas de ahorro y cuentas corrientes."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de cuentas, posiblemente vacía"
    )
    @GetMapping
    public List<CuentaResponse> listarCuentas() {
        return cuentaService.listarCuentas();
    }

    /**
     * @param id identificador de la cuenta buscada
     * @return datos de la cuenta encontrada
     */
    @Operation(summary = "Buscar una cuenta por id")
    @ApiResponse(responseCode = "200", description = "Cuenta encontrada")
    @ApiResponse(
            responseCode = "400",
            description = "El id debe ser mayor que cero",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No existe una cuenta con ese id",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @GetMapping("/{id}")
    public CuentaResponse obtenerCuentaPorId(
            @Parameter(description = "Id de la cuenta", example = "1")
            @PathVariable Long id) {

        return cuentaService.obtenerCuentaPorId(id);
    }

    /**
     * @param solicitud datos enviados en el cuerpo JSON
     * @return respuesta 201 con la cuenta creada y su ubicación
     */
    @Operation(
            summary = "Crear una cuenta",
            description = """
                    El tipo debe ser CAJA_DE_AHORRO o CUENTA_CORRIENTE.
                    La cuenta pertenece a un usuario existente.
                    El saldo inicial no puede ser negativo.
                    Para cuenta corriente se requiere limiteDescubierto,
                    que tampoco puede ser negativo.
                    La aplicación asigna fecha de alta, moneda ARS y estado ACTIVA.
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cuenta creada",
            headers = @Header(
                    name = "Location",
                    description = "Ruta para consultar la cuenta creada",
                    schema = @Schema(type = "string")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de creación inválidos",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El usuario titular no existe",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "CBU, alias o número repetido, o conflicto de integridad",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(
            @RequestBody CrearCuentaRequest solicitud) {

        CuentaResponse creada = cuentaService.crearCuenta(solicitud);

        // Informa la dirección de consulta del recurso creado.
        URI ubicacion = URI.create("/cuentas/" + creada.getId());

        return ResponseEntity.created(ubicacion).body(creada);
    }
    /**
     * Actualiza la representación editable de la cuenta: su alias.
     *
     * @param id identificador de la cuenta
     * @param solicitud nuevo alias
     * @return datos de la cuenta actualizada
     */
    @Operation(
            summary = "Actualizar el alias de una cuenta",
            description = """
                    El cuerpo contiene el alias que debe quedar en la cuenta.
                    Puede conservarse el alias actual.
                    No se modifican saldo, CBU, número ni titular.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Cuenta actualizada")
    @ApiResponse(
            responseCode = "400",
            description = "Id o alias inválido",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "La cuenta no existe",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Alias ocupado o conflicto de integridad",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @PutMapping("/{id}")
    public CuentaResponse actualizarCuenta(
            @PathVariable Long id,
            @RequestBody ActualizarCuentaRequest solicitud) {

        return cuentaService.actualizarCuenta(id, solicitud);
    }

    /**
     * Elimina una cuenta sin saldo.
     *
     * @param id identificador de la cuenta
     * @return respuesta 204 sin cuerpo
     */
    @Operation(
            summary = "Eliminar una cuenta",
            description = "Elimina físicamente la cuenta si su saldo es cero."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Cuenta eliminada; respuesta sin cuerpo",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "Id inválido",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "La cuenta no existe",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "La cuenta conserva saldo o existe un conflicto de integridad",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }
}