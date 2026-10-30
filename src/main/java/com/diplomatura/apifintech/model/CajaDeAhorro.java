package com.diplomatura.apifintech.model;

import com.diplomatura.apifintech.exception.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cajas_de_ahorro")
public class CajaDeAhorro extends Cuenta{

    protected CajaDeAhorro() {
    }

    public CajaDeAhorro(String numeroCuenta, String alias, BigDecimal saldo,
                        String cbu, LocalDate fechaAlta, Usuario usuario) {
        super(numeroCuenta, alias, saldo, cbu, fechaAlta, usuario);
    }

    /**
     * Informa el tipo correspondiente a esta clase.
     *
     * @return CAJA_DE_AHORRO
     */
    @Override
    public String getTipo() {
        return "CAJA_DE_AHORRO";
    }

    @Override
    public void retirar(BigDecimal monto) {
        if (getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se permiten retiros en cuentas activas"
            );
        }
        if (monto == null){
            throw new IllegalArgumentException("El monto es obligatorio");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        if (monto.compareTo(getSaldo()) > 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente para el retiro");
        }
        descontarSaldo(monto);
    }
}
