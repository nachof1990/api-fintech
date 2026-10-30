package com.diplomatura.apifintech.dto;

/**
 * Contiene los datos editables de una cuenta.
 * En este TP, la actualización permite cambiar únicamente el alias.
 */
public class ActualizarCuentaRequest {

    private String alias;

    /** @return nuevo alias solicitado */
    public String getAlias() {
        return alias;
    }

    /** @param alias nuevo alias solicitado */
    public void setAlias(String alias) {
        this.alias = alias;
    }
}
