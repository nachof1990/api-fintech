package com.diplomatura.apifintech.model;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.diplomatura.apifintech.exception.SaldoInsuficienteException;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class CajaDeAhorroTest {
    @Test
    void deberiaDescontarElMontoAlRetirar() {
        // Given: una cuenta con saldo inicial de 1000.
        Usuario usuario = new Usuario(
                "Ana",
                "Perez",
                "ana@example.com",
                "12345678",
                LocalDate.of(1995, 5, 20)
        );
        CajaDeAhorro cuenta = new CajaDeAhorro(
                "000001",
                "ana.ahorro",
                new BigDecimal("1000.00"),
                "0000000000000000000001",
                LocalDate.of(2026, 10, 18),
                usuario
        );
        // When: retiramos 300.
        cuenta.retirar(new BigDecimal("300.00"));
        // Then: el saldo debe quedar en 700.
        assertEquals(new BigDecimal("700.00"), cuenta.getSaldo());
    }
    @Test
    void deberiaRechazarRetiroSiElSaldoNoAlcanza() {
        // Given: una cuenta con saldo de 1000.
        Usuario usuario = new Usuario(
                "Ana", "Perez", "ana@example.com", "12345678",
                LocalDate.of(1995, 5, 20)
        );


        CajaDeAhorro cuenta = new CajaDeAhorro(
                "000001",
                "ana.ahorro",
                new BigDecimal("1000.00"),
                "0000000000000000000001",
                LocalDate.of(2026, 10, 18),
                usuario
        );


        // When y Then: el retiro debe lanzar la excepcion.
        assertThrows(
                SaldoInsuficienteException.class,
                () -> cuenta.retirar(new BigDecimal("1500.00"))
        );


        // El retiro rechazado no debe modificar el saldo.
        assertEquals(new BigDecimal("1000.00"), cuenta.getSaldo());
    }
}
