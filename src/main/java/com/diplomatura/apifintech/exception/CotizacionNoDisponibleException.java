package com.diplomatura.apifintech.exception;

/**
 * Indica que no fue posible obtener una cotización utilizable.
 */
public class CotizacionNoDisponibleException extends RuntimeException {

    /**
     * Representa una respuesta sin los datos necesarios.
     *
     * @param mensaje explicación del problema
     */
    public CotizacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    /**
     * Conserva la causa técnica de un fallo al consultar al proveedor.
     *
     * @param mensaje explicación del problema
     * @param causa excepción original
     */
    public CotizacionNoDisponibleException(
            String mensaje,
            Throwable causa) {
        super(mensaje, causa);
    }
}
