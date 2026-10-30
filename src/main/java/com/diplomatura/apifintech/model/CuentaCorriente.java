package com.diplomatura.apifintech.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.diplomatura.apifintech.exception.SaldoInsuficienteException;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "cuentas_corrientes")
public class CuentaCorriente extends Cuenta{

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limiteDescubierto;

    protected CuentaCorriente() {
        this.limiteDescubierto = BigDecimal.ZERO;
    }

    public CuentaCorriente(String numeroCuenta, String alias, BigDecimal saldo,
                           String cbu, LocalDate fechaAlta, Usuario usuario,
                           BigDecimal limiteDescubierto) {


        super(numeroCuenta, alias, saldo, cbu, fechaAlta, usuario);


        if (limiteDescubierto == null
                || limiteDescubierto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El limite de descubierto es obligatorio y no puede ser negativo"
            );
        }


        this.limiteDescubierto = limiteDescubierto;
    }

    public BigDecimal getLimiteDescubierto() {
        return limiteDescubierto;
    }

    /**
     * Informa el tipo correspondiente a esta clase.
     *
     * @return CUENTA_CORRIENTE
     */
    @Override
    public String getTipo() {
        return "CUENTA_CORRIENTE";
    }

    @Override
    public void retirar(BigDecimal monto) {
        if (getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException(
                    "Solo se permiten retiros en cuentas activas"
            );
        }


        if (monto == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }


        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        BigDecimal disponible = getSaldo().add(limiteDescubierto);
        if (monto.compareTo(disponible) > 0) {
            throw new SaldoInsuficienteException(
                    "El retiro supera el saldo y el descubierto disponible"
            );
        }
        descontarSaldo(monto);
    }
}
