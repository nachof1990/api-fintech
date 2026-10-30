package com.diplomatura.apifintech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.dao.DataIntegrityViolationException;


@RestControllerAdvice
public class ManejadorGlobalErrores {


    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail manejarUsuarioNoEncontrado(
            UsuarioNoEncontradoException excepcion) {


        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                excepcion.getMessage()
        );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail manejarArgumentoInvalido(
            IllegalArgumentException excepcion) {


        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                excepcion.getMessage()
        );
    }
    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ProblemDetail manejarCuentaNoEncontrada(
            CuentaNoEncontradaException excepcion) {


        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                excepcion.getMessage()
        );
    }
    /**
     * Convierte el conflicto de CBU duplicado en una respuesta HTTP 409.
     *
     * @param excepcion conflicto detectado al crear la cuenta
     * @return detalle del problema con estado 409
     */
    @ExceptionHandler(CbuDuplicadoException.class)
    public ProblemDetail manejarCbuDuplicado(
            CbuDuplicadoException excepcion) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                excepcion.getMessage()
        );
    }
    /**
     * Atiende operaciones rechazadas por restricciones de integridad
     * de la base de datos, incluidas las restricciones de unicidad.
     *
     * @param excepcion rechazo informado por la capa de persistencia
     * @return respuesta 409 con una explicación general del conflicto
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarConflictoDeIntegridad(
            DataIntegrityViolationException excepcion) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "La operacion entra en conflicto con una restriccion "
                        + "de integridad de los datos"
        );
    }
    /**
     * Informa como conflicto un alias o número de cuenta repetido.
     *
     * @param excepcion dato duplicado detectado por el servicio
     * @return respuesta HTTP 409 con el detalle del conflicto
     */
    @ExceptionHandler(DatoCuentaDuplicadoException.class)
    public ProblemDetail manejarDatoCuentaDuplicado(
            DatoCuentaDuplicadoException excepcion) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                excepcion.getMessage()
        );
    }

    /**
     * Informa que temporalmente no se pudo obtener la cotización.
     *
     * @param excepcion fallo detectado por el servicio de cotización
     * @return respuesta HTTP 503 con una explicación del problema
     */
    @ExceptionHandler(CotizacionNoDisponibleException.class)
    public ProblemDetail manejarCotizacionNoDisponible(
            CotizacionNoDisponibleException excepcion) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                excepcion.getMessage()
        );
    }
    /**
     * Informa el conflicto al intentar eliminar una cuenta con saldo.
     *
     * @param excepcion operación rechazada por la regla de saldo cero
     * @return respuesta HTTP 409 con la explicación
     */
    @ExceptionHandler(CuentaConSaldoException.class)
    public ProblemDetail manejarCuentaConSaldo(
            CuentaConSaldoException excepcion) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                excepcion.getMessage()
        );
    }
}
