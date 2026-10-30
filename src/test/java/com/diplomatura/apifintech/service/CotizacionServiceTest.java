package com.diplomatura.apifintech.service;

import com.diplomatura.apifintech.dto.CotizacionDolarResponse;
import com.diplomatura.apifintech.exception.CotizacionNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Comprueba la consulta de cotizaciones con respuestas HTTP simuladas.
 * No necesita Internet, MySQL ni iniciar el contexto de Spring.
 */
class CotizacionServiceTest {

    private MockRestServiceServer servidor;
    private CotizacionService cotizacionService;

    /**
     * Prepara un cliente nuevo y su proveedor simulado para cada test.
     */
    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://proveedor.test");

        // Intercepta las solicitudes del cliente: no se envían a Internet.
        servidor = MockRestServiceServer.bindTo(builder).build();

        cotizacionService = new CotizacionService(builder.build());
    }

    /**
     * Comprueba la conversión de una respuesta JSON válida al DTO.
     */
    @Test
    void deberiaDevolverLaCotizacionRecibida() {
        // Given: precios ficticios y una respuesta completa.
        String json = """
                {
                  "moneda": "USD",
                  "casa": "oficial",
                  "nombre": "Oficial",
                  "compra": 1000.00,
                  "venta": 1050.00,
                  "fechaActualizacion": "2026-10-23T16:00:00.000Z"
                }
                """;

        servidor.expect(requestTo(
                        "https://proveedor.test/v1/dolares/oficial"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        // When: ejecutamos el servicio real.
        CotizacionDolarResponse resultado =
                cotizacionService.obtenerDolarOficial();

        // Then: conserva los datos informados por el proveedor.
        assertEquals("USD", resultado.getMoneda());
        assertEquals("oficial", resultado.getCasa());
        assertEquals("Oficial", resultado.getNombre());

        // compareTo comprueba el importe sin exigir una escala concreta.
        assertEquals(
                0,
                new BigDecimal("1000.00").compareTo(resultado.getCompra())
        );
        assertEquals(
                0,
                new BigDecimal("1050.00").compareTo(resultado.getVenta())
        );
        assertEquals(
                "2026-10-23T16:00:00.000Z",
                resultado.getFechaActualizacion()
        );

        servidor.verify();
    }

    /**
     * Comprueba que no se acepte una cotización sin precio de compra.
     */
    @Test
    void deberiaRechazarUnaCotizacionIncompleta() {
        // Given: falta el campo compra.
        String json = """
                {
                  "moneda": "USD",
                  "casa": "oficial",
                  "venta": 1050.00
                }
                """;

        servidor.expect(requestTo(
                        "https://proveedor.test/v1/dolares/oficial"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        // When + Then: el servicio debe rechazar los datos incompletos.
        CotizacionNoDisponibleException excepcion = assertThrows(
                CotizacionNoDisponibleException.class,
                () -> cotizacionService.obtenerDolarOficial()
        );

        assertEquals(
                "El proveedor no devolvio una cotizacion completa",
                excepcion.getMessage()
        );

        servidor.verify();
    }

    /**
     * Comprueba el rechazo de una respuesta sin cuerpo.
     */
    @Test
    void deberiaRechazarUnaRespuestaVacia() {
        // Given: el proveedor responde sin contenido.
        servidor.expect(requestTo(
                        "https://proveedor.test/v1/dolares/oficial"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        // When + Then: no debemos devolver una cotización inexistente.
        CotizacionNoDisponibleException excepcion = assertThrows(
                CotizacionNoDisponibleException.class,
                () -> cotizacionService.obtenerDolarOficial()
        );

        assertEquals(
                "El proveedor no devolvio una cotizacion completa",
                excepcion.getMessage()
        );

        servidor.verify();
    }

    /**
     * Comprueba que el fallo HTTP del proveedor se traduzca
     * a la excepción propia y conserve su causa técnica.
     */
    @Test
    void deberiaInformarElFalloDelProveedor() {
        // Given: el proveedor responde con HTTP 500.
        servidor.expect(requestTo(
                        "https://proveedor.test/v1/dolares/oficial"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // When + Then: se ejecuta el tratamiento del fallo en el servicio.
        CotizacionNoDisponibleException excepcion = assertThrows(
                CotizacionNoDisponibleException.class,
                () -> cotizacionService.obtenerDolarOficial()
        );

        assertEquals(
                "No fue posible consultar la cotizacion del dolar",
                excepcion.getMessage()
        );
        assertNotNull(excepcion.getCause());

        servidor.verify();
    }
}
