package com.diplomatura.apifintech.dto;

import com.diplomatura.apifintech.model.EstadoCuenta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reúne los datos de una cuenta que la API devolverá al cliente.
 * Representa al titular mediante su id, sin incluir el objeto Usuario.
 */
public class CuentaResponse {

    private Long id;

    // Identifica si es CAJA_DE_AHORRO o CUENTA_CORRIENTE.
    private String tipo;

    private String numeroCuenta;
    private String alias;
    private String cbu;
    private BigDecimal saldo;
    private LocalDate fechaAlta;
    private String moneda;
    private EstadoCuenta estado;

    // Identifica al único titular de la cuenta.
    private Long usuarioId;

    // Solo corresponde a cuenta corriente; para caja de ahorro será null.
    private BigDecimal limiteDescubierto;

    /**
     * Construye la respuesta con los datos seleccionados de una cuenta.
     *
     * @param id identificador de la cuenta
     * @param tipo CAJA_DE_AHORRO o CUENTA_CORRIENTE
     * @param numeroCuenta número que identifica la cuenta
     * @param alias alias de la cuenta
     * @param cbu CBU de la cuenta
     * @param saldo saldo actual
     * @param fechaAlta fecha de creación
     * @param moneda moneda de la cuenta
     * @param estado estado actual de la cuenta
     * @param usuarioId identificador del titular
     * @param limiteDescubierto límite de corriente; null para caja de ahorro
     */
    public CuentaResponse(
            Long id,
            String tipo,
            String numeroCuenta,
            String alias,
            String cbu,
            BigDecimal saldo,
            LocalDate fechaAlta,
            String moneda,
            EstadoCuenta estado,
            Long usuarioId,
            BigDecimal limiteDescubierto) {

        this.id = id;
        this.tipo = tipo;
        this.numeroCuenta = numeroCuenta;
        this.alias = alias;
        this.cbu = cbu;
        this.saldo = saldo;
        this.fechaAlta = fechaAlta;
        this.moneda = moneda;
        this.estado = estado;
        this.usuarioId = usuarioId;
        this.limiteDescubierto = limiteDescubierto;
    }

    /**
     * @return identificador de la cuenta
     */
    public Long getId() {
        return id;
    }

    /**
     * @return tipo de cuenta: CAJA_DE_AHORRO o CUENTA_CORRIENTE
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @return número de la cuenta
     */
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    /**
     * @return alias de la cuenta
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return CBU de la cuenta
     */
    public String getCbu() {
        return cbu;
    }

    /**
     * @return saldo actual de la cuenta
     */
    public BigDecimal getSaldo() {
        return saldo;
    }

    /**
     * @return fecha de creación de la cuenta
     */
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    /**
     * @return moneda de la cuenta
     */
    public String getMoneda() {
        return moneda;
    }

    /**
     * @return estado actual de la cuenta
     */
    public EstadoCuenta getEstado() {
        return estado;
    }

    /**
     * @return identificador del único titular
     */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /**
     * @return límite de descubierto; null si es una caja de ahorro
     */
    public BigDecimal getLimiteDescubierto() {
        return limiteDescubierto;
    }
}
