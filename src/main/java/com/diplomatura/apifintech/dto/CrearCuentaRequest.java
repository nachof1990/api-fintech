package com.diplomatura.apifintech.dto;


import java.math.BigDecimal;


public class CrearCuentaRequest {


    private String tipo;
    private String numeroCuenta;
    private String alias;
    private String cbu;
    private BigDecimal saldoInicial;
    private Long usuarioId;
    private BigDecimal limiteDescubierto;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getLimiteDescubierto() {
        return limiteDescubierto;
    }

    public void setLimiteDescubierto(BigDecimal limiteDescubierto) {
        this.limiteDescubierto = limiteDescubierto;
    }
}