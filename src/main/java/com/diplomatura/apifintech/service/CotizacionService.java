package com.diplomatura.apifintech.service;

import com.diplomatura.apifintech.dto.CotizacionDolarResponse;
import com.diplomatura.apifintech.exception.CotizacionNoDisponibleException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consulta al proveedor externo y controla los fallos de comunicación.
 */
@Service
public class CotizacionService {

    private final RestClient dolarRestClient;

    /**
     * Recibe el cliente HTTP preparado por la configuración.
     *
     * @param dolarRestClient cliente para consultar DolarAPI
     */
    public CotizacionService(RestClient dolarRestClient) {
        this.dolarRestClient = dolarRestClient;
    }

    /**
     * Obtiene la cotización del dólar oficial.
     *
     * @return cotización con precios de compra y venta
     * @throws CotizacionNoDisponibleException si la consulta falla
     *         o faltan los precios en la respuesta
     */
    public CotizacionDolarResponse obtenerDolarOficial() {
        try {
            CotizacionDolarResponse cotizacion = dolarRestClient.get()
                    .uri("/v1/dolares/oficial")
                    .retrieve()
                    .body(CotizacionDolarResponse.class);

            // No devolvemos éxito si el proveedor no entrega los precios.
            if (cotizacion == null
                    || cotizacion.getCompra() == null
                    || cotizacion.getVenta() == null) {
                throw new CotizacionNoDisponibleException(
                        "El proveedor no devolvio una cotizacion completa"
                );
            }

            return cotizacion;

        } catch (RestClientException excepcion) {
            // Traduce el fallo de comunicación a una excepción del proyecto.
            throw new CotizacionNoDisponibleException(
                    "No fue posible consultar la cotizacion del dolar",
                    excepcion
            );
        }
    }
}
