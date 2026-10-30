package com.diplomatura.apifintech.dto;

import java.math.BigDecimal;

/**
 * Representa los datos de cotización recibidos del proveedor externo
 * y devueltos por nuestra API.
 */
public class CotizacionDolarResponse {

    private String moneda;
    private String casa;
    private String nombre;
    private BigDecimal compra;
    private BigDecimal venta;

    // Conserva la fecha y hora con el formato enviado por el proveedor.
    private String fechaActualizacion;

    /** @return moneda cotizada */
    public String getMoneda() {
        return moneda;
    }

    /** @param moneda moneda recibida del proveedor */
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    /** @return mercado al que pertenece la cotización */
    public String getCasa() {
        return casa;
    }

    /** @param casa mercado informado por el proveedor */
    public void setCasa(String casa) {
        this.casa = casa;
    }

    /** @return nombre de la cotización */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre informado por el proveedor */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return precio de compra */
    public BigDecimal getCompra() {
        return compra;
    }

    /** @param compra precio de compra recibido */
    public void setCompra(BigDecimal compra) {
        this.compra = compra;
    }

    /** @return precio de venta */
    public BigDecimal getVenta() {
        return venta;
    }

    /** @param venta precio de venta recibido */
    public void setVenta(BigDecimal venta) {
        this.venta = venta;
    }

    /** @return fecha y hora de actualización informada */
    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    /** @param fechaActualizacion fecha y hora recibida */
    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
