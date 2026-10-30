package com.diplomatura.apifintech.service;

import com.diplomatura.apifintech.exception.CuentaNoEncontradaException;
import com.diplomatura.apifintech.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.diplomatura.apifintech.dto.CrearCuentaRequest;
import com.diplomatura.apifintech.exception.UsuarioNoEncontradoException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import com.diplomatura.apifintech.dto.CuentaResponse;
import com.diplomatura.apifintech.model.CajaDeAhorro;
import com.diplomatura.apifintech.model.Usuario;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.diplomatura.apifintech.model.CuentaCorriente;

import static org.mockito.Mockito.verifyNoInteractions;

import com.diplomatura.apifintech.exception.CbuDuplicadoException;

import com.diplomatura.apifintech.exception.DatoCuentaDuplicadoException;
/**
 * Comprueba el comportamiento de CuentaService con dependencias simuladas.
 * Estas pruebas no necesitan iniciar Spring ni conectarse a MySQL.
 */
@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CuentaService cuentaService;

    /**
     * Comprueba que una búsqueda sin resultado lance la excepción propia
     * e informe el identificador de la cuenta buscada.
     */
    @Test
    void deberiaLanzarExcepcionSiLaCuentaNoExiste() {
        // Given: el repositorio simulado no encuentra esa cuenta.
        Long id = 999999L;
        when(cuentaRepository.findById(id))
                .thenReturn(Optional.empty());

        // When: buscamos la cuenta. Then: esperamos la excepción.
        CuentaNoEncontradaException excepcion = assertThrows(
                CuentaNoEncontradaException.class,
                () -> cuentaService.obtenerCuentaPorId(id)
        );

        assertEquals(
                "No se encontro la cuenta con id: 999999",
                excepcion.getMessage()
        );

        // Comprueba que el servicio consultó el repositorio una vez.
        verify(cuentaRepository).findById(id);
    }

    /**
     * Comprueba que una cuenta no se guarde cuando el titular no existe.
     */
    @Test
    void deberiaRechazarCreacionSinGuardarSiElUsuarioNoExiste() {
        // Given: datos válidos para llegar a la búsqueda del titular.
        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000003");
        solicitud.setAlias("prueba.usuario.inexistente");
        solicitud.setCbu("0000000000000000000003");
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(999999L);

        // Simula el rechazo de UsuarioService al buscar ese titular.
        when(usuarioService.obtenerUsuarioPorId(999999L))
                .thenThrow(new UsuarioNoEncontradoException(999999L));

        // When + Then: la creación debe propagar la excepción.
        assertThrows(
                UsuarioNoEncontradoException.class,
                () -> cuentaService.crearCuenta(solicitud)
        );

        // La solicitud rechazada nunca debe llegar al guardado.
        verify(cuentaRepository, never()).save(any());
    }
    /**
     * Comprueba la creación de una caja de ahorro para un titular existente
     * y los datos principales de su respuesta.
     */
    @Test
    void deberiaCrearCajaDeAhorroYDevolverSuRespuesta() {
        // Given: simulamos un titular existente con id 1.
        Usuario titular = mock(Usuario.class);
        when(titular.getId()).thenReturn(1L);
        when(usuarioService.obtenerUsuarioPorId(1L))
                .thenReturn(titular);

        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000003");
        solicitud.setAlias("ana.ahorro.test");
        solicitud.setCbu("0000000000000000000003");
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(1L);

        // El repositorio simulado devuelve la misma cuenta que recibe.
        when(cuentaRepository.save(any(CajaDeAhorro.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // When: ejecutamos la creación en el servicio real.
        CuentaResponse respuesta = cuentaService.crearCuenta(solicitud);

        // Then: comprobamos los datos copiados al DTO.
        assertEquals("CAJA_DE_AHORRO", respuesta.getTipo());
        assertEquals("000003", respuesta.getNumeroCuenta());
        assertEquals("ana.ahorro.test", respuesta.getAlias());
        assertEquals(
                "0000000000000000000003",
                respuesta.getCbu()
        );
        assertEquals(
                new BigDecimal("1000.00"),
                respuesta.getSaldo()
        );
        assertEquals(1L, respuesta.getUsuarioId());
        assertNull(respuesta.getLimiteDescubierto());

        // Debe guardar una caja de ahorro exactamente una vez.
        verify(cuentaRepository).save(any(CajaDeAhorro.class));
    }

    /**
     * Comprueba que la creación de una cuenta corriente conserve
     * el saldo y el límite de descubierto como datos separados.
     */
    @Test
    void deberiaCrearCuentaCorrienteYDevolverSuDescubierto() {
        // Given: un titular existente y una solicitud de cuenta corriente.
        Usuario titular = mock(Usuario.class);
        when(titular.getId()).thenReturn(1L);
        when(usuarioService.obtenerUsuarioPorId(1L))
                .thenReturn(titular);

        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CUENTA_CORRIENTE");
        solicitud.setNumeroCuenta("000004");
        solicitud.setAlias("ana.corriente.test");
        solicitud.setCbu("0000000000000000000004");
        solicitud.setSaldoInicial(new BigDecimal("2000.00"));
        solicitud.setUsuarioId(1L);
        solicitud.setLimiteDescubierto(new BigDecimal("500.00"));

        // Devuelve la cuenta recibida, sin conectarse a una base de datos.
        when(cuentaRepository.save(any(CuentaCorriente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // When: el servicio construye la cuenta y prepara su respuesta.
        CuentaResponse respuesta = cuentaService.crearCuenta(solicitud);

        // Then: el descubierto debe aparecer sin sumarse al saldo.
        assertEquals("CUENTA_CORRIENTE", respuesta.getTipo());
        assertEquals(
                new BigDecimal("2000.00"),
                respuesta.getSaldo()
        );
        assertEquals(
                new BigDecimal("500.00"),
                respuesta.getLimiteDescubierto()
        );
        assertEquals(1L, respuesta.getUsuarioId());

        verify(cuentaRepository).save(any(CuentaCorriente.class));
    }
    /**
     * Comprueba que un CBU inválido interrumpa la creación antes
     * de buscar al titular o intentar guardar la cuenta.
     */
    @Test
    void deberiaRechazarCbuInvalidoSinBuscarUsuarioNiGuardar() {
        // Given: el dato inválido de esta solicitud es el CBU.
        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000005");
        solicitud.setAlias("prueba.cbu.invalido");
        solicitud.setCbu("123");
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(1L);

        // When + Then: la validación debe rechazar la solicitud.
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> cuentaService.crearCuenta(solicitud)
        );

        assertEquals(
                "El CBU debe contener exactamente 22 digitos",
                excepcion.getMessage()
        );

        // El rechazo debe ocurrir antes de consultar al titular.
        verifyNoInteractions(usuarioService);

        // No debe intentar guardar ninguna cuenta.
        verify(cuentaRepository, never()).save(any());
    }

    /**
     * Comprueba que un CBU registrado produzca un conflicto
     * antes de buscar al titular o guardar la cuenta.
     */
    @Test
    void deberiaRechazarCbuDuplicadoSinGuardar() {
        // Given: solicitud válida cuyo CBU ya existe.
        String cbu = "0000000000000000000001";

        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000010");
        solicitud.setAlias("prueba.cbu.duplicado");
        solicitud.setCbu(cbu);
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(1L);

        when(cuentaRepository.existsByCbu(cbu)).thenReturn(true);

        // When + Then: el servicio debe rechazar la creación.
        CbuDuplicadoException excepcion = assertThrows(
                CbuDuplicadoException.class,
                () -> cuentaService.crearCuenta(solicitud)
        );

        assertEquals(
                "Ya existe una cuenta con el CBU: " + cbu,
                excepcion.getMessage()
        );

        verify(cuentaRepository).existsByCbu(cbu);
        verifyNoInteractions(usuarioService);
        verify(cuentaRepository, never()).save(any());
    }

    /**
     * Comprueba que un alias registrado impida guardar la cuenta.
     */
    @Test
    void deberiaRechazarAliasDuplicadoSinGuardar() {
        // Given: CBU disponible, pero alias registrado.
        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000011");
        solicitud.setAlias("ana.ahorro.tp");
        solicitud.setCbu("0000000000000000000011");
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(1L);

        when(cuentaRepository.existsByAlias("ana.ahorro.tp"))
                .thenReturn(true);

        // When + Then: se informa el conflicto sin intentar guardar.
        DatoCuentaDuplicadoException excepcion = assertThrows(
                DatoCuentaDuplicadoException.class,
                () -> cuentaService.crearCuenta(solicitud)
        );

        assertEquals(
                "Ya existe una cuenta con alias: ana.ahorro.tp",
                excepcion.getMessage()
        );

        verifyNoInteractions(usuarioService);
        verify(cuentaRepository, never()).save(any());
    }

    /**
     * Comprueba que un número registrado impida guardar la cuenta.
     */
    @Test
    void deberiaRechazarNumeroCuentaDuplicadoSinGuardar() {
        // Given: CBU y alias disponibles, pero número registrado.
        CrearCuentaRequest solicitud = new CrearCuentaRequest();
        solicitud.setTipo("CAJA_DE_AHORRO");
        solicitud.setNumeroCuenta("000001");
        solicitud.setAlias("prueba.numero.duplicado");
        solicitud.setCbu("0000000000000000000011");
        solicitud.setSaldoInicial(new BigDecimal("1000.00"));
        solicitud.setUsuarioId(1L);

        when(cuentaRepository.existsByNumeroCuenta("000001"))
                .thenReturn(true);

        // When + Then: se informa el conflicto sin intentar guardar.
        DatoCuentaDuplicadoException excepcion = assertThrows(
                DatoCuentaDuplicadoException.class,
                () -> cuentaService.crearCuenta(solicitud)
        );

        assertEquals(
                "Ya existe una cuenta con numero de cuenta: 000001",
                excepcion.getMessage()
        );

        verifyNoInteractions(usuarioService);
        verify(cuentaRepository, never()).save(any());
    }
}
