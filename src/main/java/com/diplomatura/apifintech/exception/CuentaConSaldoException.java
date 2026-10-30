package com.diplomatura.apifintech.exception;

/**
 * Indica que una cuenta no puede eliminarse porque conserva saldo.
 */
public class CuentaConSaldoException extends RuntimeException {

    /**
     * @param id identificador de la cuenta que se intentó eliminar
     */
    public CuentaConSaldoException(Long id) {
        super("No se puede eliminar la cuenta " + id
                + " porque su saldo no es cero");
    }
}
