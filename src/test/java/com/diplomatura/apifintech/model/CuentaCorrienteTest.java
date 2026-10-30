package com.diplomatura.apifintech.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.diplomatura.apifintech.exception.SaldoInsuficienteException;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class CuentaCorrienteTest {

    @Test
    void deberiaPermitirRetirarUsandoElDescubierto() {
        // Given: saldo de 1000 y limite de descubierto de 500.
        Usuario usuario = new Usuario(
                "Ana", "Perez", "ana@example.com", "12345678",
                LocalDate.of(1995, 5, 20)
        );

        CuentaCorriente cuenta = new CuentaCorriente(
                "000002",
                "ana.corriente",
                new BigDecimal("1000.00"),
                "0000000000000000000002",
                LocalDate.of(2026, 10, 19),
                usuario,
                new BigDecimal("500.00")
        );

        // When: retiramos mas que el saldo, dentro del limite.
        cuenta.retirar(new BigDecimal("1300.00"));

        // Then: el saldo queda negativo en 300.
        assertEquals(new BigDecimal("-300.00"), cuenta.getSaldo());
    }

    @Test
    void deberiaRechazarRetiroQueSupereElDescubierto() {
        // Given: saldo de 1000 y descubierto de 500.
        Usuario usuario = new Usuario(
                "Ana", "Perez", "ana@example.com", "12345678",
                LocalDate.of(1995, 5, 20)
        );

        CuentaCorriente cuenta = new CuentaCorriente(
                "000002",
                "ana.corriente",
                new BigDecimal("1000.00"),
                "0000000000000000000002",
                LocalDate.of(2026, 10, 19),
                usuario,
                new BigDecimal("500.00")
        );

        // When y Then: retirar 1600 supera los 1500 disponibles.
        assertThrows(
                SaldoInsuficienteException.class,
                () -> cuenta.retirar(new BigDecimal("1600.00"))
        );

        // El rechazo no debe modificar el saldo.
        assertEquals(new BigDecimal("1000.00"), cuenta.getSaldo());
    }
}