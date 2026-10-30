package com.diplomatura.apifintech.controller;

import com.diplomatura.apifintech.dto.CotizacionDolarResponse;
import com.diplomatura.apifintech.service.CotizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la consulta de cotización del dólar oficial.
 */
@RestController
@RequestMapping("/cotizaciones")
@Tag(name = "Cotizaciones", description = "Consulta del dólar mediante DolarAPI")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    /**
     * @param cotizacionService servicio que consulta al proveedor externo
     */
    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    /**
     * @return cotización informada por el proveedor externo
     */
    @Operation(
            summary = "Consultar el dólar oficial",
            description = """
                    Obtiene de DolarAPI los precios de compra y venta,
                    la moneda y la fecha de actualización del proveedor.
                    Los valores pueden cambiar entre consultas.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cotización obtenida correctamente"
    )
    @ApiResponse(
            responseCode = "503",
            description = "No se pudo obtener una cotización completa",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @GetMapping("/dolar/oficial")
    public CotizacionDolarResponse obtenerDolarOficial() {
        return cotizacionService.obtenerDolarOficial();
    }
}
