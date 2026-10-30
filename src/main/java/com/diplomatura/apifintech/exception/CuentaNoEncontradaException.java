package com.diplomatura.apifintech.exception;


public class CuentaNoEncontradaException extends RuntimeException {


    public CuentaNoEncontradaException(Long id) {
        super("No se encontro la cuenta con id: " + id);
    }
}
