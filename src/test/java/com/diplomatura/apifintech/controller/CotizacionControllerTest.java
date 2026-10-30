package com.diplomatura.apifintech.controller;

import com.diplomatura.apifintech.exception.CotizacionNoDisponibleException;
import com.diplomatura.apifintech.exception.ManejadorGlobalErrores;
import com.diplomatura.apifintech.service.CotizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba la respuesta HTTP ante un fallo del servicio de cotización.
 * Usa el controlador y el manejador reales, con el servicio simulado.
 */
@ExtendWith(MockitoExtension.class)
class CotizacionControllerTest {

    @Mock
    private CotizacionService cotizacionService;

    private MockMvc mockMvc;

    /**
     * Prepara el controlador y registra explícitamente su manejador global.
     */
    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CotizacionController(cotizacionService))
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    /**
     * Comprueba que la excepción del servicio produzca un ProblemDetail
     * con estado HTTP 503.
     */
    @Test
    void deberiaResponder503CuandoLaCotizacionNoEstaDisponible()
            throws Exception {

        // Given: el servicio no puede obtener la cotización.
        String mensaje =
                "No fue posible consultar la cotizacion del dolar";

        when(cotizacionService.obtenerDolarOficial())
                .thenThrow(new CotizacionNoDisponibleException(mensaje));

        // When + Then: verificamos estado HTTP y contenido JSON.
        mockMvc.perform(get("/cotizaciones/dolar/oficial"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value(mensaje))
                .andExpect(jsonPath("$.instance")
                        .value("/cotizaciones/dolar/oficial"));

        verify(cotizacionService).obtenerDolarOficial();
    }
}