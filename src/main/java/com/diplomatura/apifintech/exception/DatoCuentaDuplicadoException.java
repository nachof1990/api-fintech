package com.diplomatura.apifintech.exception;

/**
 * Indica que un alias o número solicitado ya pertenece a otra cuenta.
 */
public class DatoCuentaDuplicadoException extends RuntimeException {

    /**
     * Construye un mensaje que identifica el dato repetido.
     *
     * @param campo nombre del dato: alias o numero de cuenta
     * @param valor valor que ya está registrado
     */
    public DatoCuentaDuplicadoException(String campo, String valor) {
        super("Ya existe una cuenta con " + campo + ": " + valor);
    }
}
