package com.diplomatura.apifintech.exception;

/**
 * Indica que se intentó crear una cuenta con un CBU ya registrado.
 */
public class CbuDuplicadoException extends RuntimeException {

    /**
     * Construye la excepción con el CBU que produjo el conflicto.
     *
     * @param cbu CBU que ya pertenece a una cuenta
     */
    public CbuDuplicadoException(String cbu) {
        super("Ya existe una cuenta con el CBU: " + cbu);
    }
}
