package com.diplomatura.apifintech.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
        name = "cuentas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cuentas_cbu",
                        columnNames = "cbu"
                ),
                @UniqueConstraint(
                        name = "uk_cuentas_alias",
                        columnNames = "alias"
                ),
                @UniqueConstraint(
                        name = "uk_cuentas_numero",
                        columnNames = "numero_cuenta"
                )
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numeroCuenta;
    private String alias;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;
    private String cbu;
    private LocalDate fechaAlta;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    private String moneda = "ARS";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    protected Cuenta() {
    }

    protected Cuenta(String numeroCuenta, String alias, BigDecimal saldo, String cbu, LocalDate fechaAlta, Usuario usuario) {
        if (saldo == null || saldo.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException(
                    "El saldo inicial es obligatorio y no puede ser negativo"
            );
        }
        this.numeroCuenta = numeroCuenta;
        this.alias = alias;
        this.saldo = saldo;
        this.cbu = cbu;
        this.fechaAlta = fechaAlta;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getAlias() {
        return alias;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getCbu() {
        return cbu;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void depositar(BigDecimal monto){
        if (monto == null){
            throw new IllegalArgumentException("El monto es obligatorio");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        saldo = saldo.add(monto);
    }
    protected void descontarSaldo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }


        saldo = saldo.subtract(monto);
    }
    public abstract void retirar(BigDecimal monto);

    /**
     * Cada clase hija debe informar su tipo de cuenta.
     *
     * @return identificador del tipo de cuenta
     */
    public abstract String getTipo();

}
