package com.diplomatura.apifintech.service;

import com.diplomatura.apifintech.dto.ActualizarCuentaRequest;
import com.diplomatura.apifintech.dto.CuentaResponse;
import com.diplomatura.apifintech.exception.CuentaConSaldoException;
import com.diplomatura.apifintech.exception.CuentaNoEncontradaException;
import com.diplomatura.apifintech.exception.DatoCuentaDuplicadoException;
import com.diplomatura.apifintech.model.CajaDeAhorro;
import com.diplomatura.apifintech.model.Cuenta;
import com.diplomatura.apifintech.model.Usuario;
import com.diplomatura.apifintech.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Comprueba la actualización del alias y la eliminación de cuentas.
 * No necesita Spring, MySQL ni Internet.
 */
@ExtendWith(MockitoExtension.class)
class CuentaEdicionServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CuentaService cuentaService;

    /**
     * Comprueba que cambiar el alias conserve los otros datos.
     */
    @Test
    void deberiaActualizarElAlias() {
        Cuenta cuenta = cuentaConSaldo("1000.00");
        ActualizarCuentaRequest solicitud = solicitudConAlias("alias.nuevo");

        when(cuentaRepository.findById(1L))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(cuenta)).thenReturn(cuenta);

        CuentaResponse respuesta =
                cuentaService.actualizarCuenta(1L, solicitud);

        assertEquals("alias.nuevo", respuesta.getAlias());
        assertEquals("000030", respuesta.getNumeroCuenta());
        assertEquals(
                "0000000000000000000030",
                respuesta.getCbu()
        );
        assertEquals(new BigDecimal("1000.00"), respuesta.getSaldo());

        verify(cuentaRepository)
                .existsByAliasAndIdNot("alias.nuevo", 1L);
        verify(cuentaRepository).save(cuenta);
    }

    /**
     * Comprueba que conservar el propio alias sea una actualización válida.
     */
    @Test
    void deberiaPermitirConservarElAliasActual() {
        Cuenta cuenta = cuentaConSaldo("0.00");
        ActualizarCuentaRequest solicitud =
                solicitudConAlias("bonus.original");

        when(cuentaRepository.findById(1L))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(cuenta)).thenReturn(cuenta);

        CuentaResponse respuesta =
                cuentaService.actualizarCuenta(1L, solicitud);

        assertEquals("bonus.original", respuesta.getAlias());

        verify(cuentaRepository)
                .existsByAliasAndIdNot("bonus.original", 1L);
        verify(cuentaRepository).save(cuenta);
    }

    /**
     * Comprueba que el conflicto no cambie la entidad ni llegue al guardado.
     */
    @Test
    void deberiaRechazarElAliasDeOtraCuenta() {
        Cuenta cuenta = cuentaConSaldo("0.00");
        ActualizarCuentaRequest solicitud =
                solicitudConAlias("alias.ocupado");

        when(cuentaRepository.findById(1L))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.existsByAliasAndIdNot("alias.ocupado", 1L))
                .thenReturn(true);

        assertThrows(
                DatoCuentaDuplicadoException.class,
                () -> cuentaService.actualizarCuenta(1L, solicitud)
        );

        assertEquals("bonus.original", cuenta.getAlias());
        verify(cuentaRepository, never()).save(any());
    }

    /**
     * Comprueba que un alias en blanco se rechace antes de consultar la base.
     */
    @Test
    void deberiaRechazarUnAliasEnBlanco() {
        ActualizarCuentaRequest solicitud = solicitudConAlias("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> cuentaService.actualizarCuenta(1L, solicitud)
        );

        verifyNoInteractions(cuentaRepository, usuarioService);
    }

    /**
     * Comprueba la actualización de una cuenta inexistente.
     */
    @Test
    void deberiaRechazarActualizacionDeCuentaInexistente() {
        ActualizarCuentaRequest solicitud = solicitudConAlias("alias.nuevo");

        when(cuentaRepository.findById(999999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CuentaNoEncontradaException.class,
                () -> cuentaService.actualizarCuenta(999999L, solicitud)
        );

        verify(cuentaRepository, never()).save(any());
    }

    /**
     * Comprueba que una cuenta con saldo cero pueda eliminarse.
     */
    @Test
    void deberiaEliminarUnaCuentaSinSaldo() {
        Cuenta cuenta = cuentaConSaldo("0.00");

        when(cuentaRepository.findById(1L))
                .thenReturn(Optional.of(cuenta));

        cuentaService.eliminarCuenta(1L);

        verify(cuentaRepository).delete(cuenta);
    }

    /**
     * Comprueba que una cuenta con dinero no llegue al borrado.
     */
    @Test
    void deberiaRechazarEliminacionDeCuentaConSaldo() {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        when(cuentaRepository.findById(1L))
                .thenReturn(Optional.of(cuenta));

        assertThrows(
                CuentaConSaldoException.class,
                () -> cuentaService.eliminarCuenta(1L)
        );

        verify(cuentaRepository, never()).delete(any());
    }

    /**
     * Comprueba que eliminar una cuenta inexistente informe el error.
     */
    @Test
    void deberiaRechazarEliminacionDeCuentaInexistente() {
        when(cuentaRepository.findById(999999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CuentaNoEncontradaException.class,
                () -> cuentaService.eliminarCuenta(999999L)
        );

        verify(cuentaRepository, never()).delete(any());
    }

    /**
     * Construye una entidad real para comprobar sus cambios.
     *
     * @param saldo saldo inicial expresado como texto decimal
     * @return caja de ahorro de prueba
     */
    private CajaDeAhorro cuentaConSaldo(String saldo) {
        return new CajaDeAhorro(
                "000030",
                "bonus.original",
                new BigDecimal(saldo),
                "0000000000000000000030",
                LocalDate.of(2026, 10, 23),
                new Usuario()
        );
    }

    /**
     * @param alias alias solicitado en la actualización
     * @return solicitud preparada para el test
     */
    private ActualizarCuentaRequest solicitudConAlias(String alias) {
        ActualizarCuentaRequest solicitud = new ActualizarCuentaRequest();
        solicitud.setAlias(alias);
        return solicitud;
    }
}