package com.diplomatura.apifintech.exception;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String mensaje){
        super(mensaje);
    }
}
