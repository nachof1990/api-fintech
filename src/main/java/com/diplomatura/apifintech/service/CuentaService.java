package com.diplomatura.apifintech.service;


import com.diplomatura.apifintech.exception.CuentaNoEncontradaException;
import com.diplomatura.apifintech.model.Cuenta;
import com.diplomatura.apifintech.repository.CuentaRepository;
import org.springframework.stereotype.Service;


import java.util.List;

import com.diplomatura.apifintech.dto.CrearCuentaRequest;
import com.diplomatura.apifintech.model.Usuario;

import com.diplomatura.apifintech.model.CajaDeAhorro;
import com.diplomatura.apifintech.model.CuentaCorriente;
import java.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;
import com.diplomatura.apifintech.dto.CuentaResponse;
import java.math.BigDecimal;

import java.util.ArrayList;

import com.diplomatura.apifintech.exception.CbuDuplicadoException;

import com.diplomatura.apifintech.exception.DatoCuentaDuplicadoException;
/**
 * Coordina las consultas y la creación de cuentas.
 * Las operaciones de consulta usan transacciones de solo lectura.
 */
@Service
@Transactional(readOnly = true)
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioService usuarioService;

    public CuentaService(CuentaRepository cuentaRepository,
                         UsuarioService usuarioService) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Consulta las cuentas y prepara sus datos para la respuesta de la API.
     *
     * @return lista de respuestas; estará vacía si no hay cuentas
     */
    public List<CuentaResponse> listarCuentas() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        List<CuentaResponse> respuestas = new ArrayList<>();

        // Convierte cada entidad mientras la transacción sigue activa.
        for (Cuenta cuenta : cuentas) {
            respuestas.add(convertirARespuesta(cuenta));
        }

        return respuestas;
    }

    /**
     * Busca una cuenta por su id y prepara su respuesta.
     *
     * @param id identificador de la cuenta; debe ser mayor que cero
     * @return respuesta con los datos de la cuenta encontrada
     * @throws IllegalArgumentException si el id es nulo o no es positivo
     * @throws CuentaNoEncontradaException si la cuenta no existe
     */
    public CuentaResponse obtenerCuentaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El id de la cuenta debe ser mayor que cero"
            );
        }

        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));

        return convertirARespuesta(cuenta);
    }

    /**
     * Crea una cuenta y prepara la respuesta con sus datos guardados.
     *
     * @param solicitud datos necesarios para crear la cuenta
     * @return respuesta con los datos de la cuenta creada
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws CbuDuplicadoException si el CBU ya está registrado
     */
    @Transactional
    public CuentaResponse crearCuenta(CrearCuentaRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Los datos de la cuenta son obligatorios"
            );
        }

        if (solicitud.getNumeroCuenta() == null
                || solicitud.getNumeroCuenta().isBlank()) {
            throw new IllegalArgumentException(
                    "El numero de cuenta es obligatorio"
            );
        }

        if (solicitud.getAlias() == null
                || solicitud.getAlias().isBlank()) {
            throw new IllegalArgumentException("El alias es obligatorio");
        }

        if (solicitud.getCbu() == null
                || !solicitud.getCbu().matches("[0-9]{22}")) {
            throw new IllegalArgumentException(
                    "El CBU debe contener exactamente 22 digitos"
            );
        }

        // Comprueba los datos únicos antes de buscar al titular y guardar.
        validarDatosUnicos(solicitud);

        Usuario titular = usuarioService.obtenerUsuarioPorId(
                solicitud.getUsuarioId()
        );

        if (solicitud.getTipo() == null) {
            throw new IllegalArgumentException(
                    "El tipo de cuenta es obligatorio"
            );
        }

        Cuenta cuenta;

        switch (solicitud.getTipo()) {
            case "CAJA_DE_AHORRO":
                cuenta = new CajaDeAhorro(
                        solicitud.getNumeroCuenta(),
                        solicitud.getAlias(),
                        solicitud.getSaldoInicial(),
                        solicitud.getCbu(),
                        LocalDate.now(),
                        titular
                );
                break;

            case "CUENTA_CORRIENTE":
                cuenta = new CuentaCorriente(
                        solicitud.getNumeroCuenta(),
                        solicitud.getAlias(),
                        solicitud.getSaldoInicial(),
                        solicitud.getCbu(),
                        LocalDate.now(),
                        titular,
                        solicitud.getLimiteDescubierto()
                );
                break;

            default:
                throw new IllegalArgumentException(
                        "El tipo de cuenta no es valido"
                );
        }

        // Guarda la entidad y usa el resultado para construir la respuesta.
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);
        return convertirARespuesta(cuentaGuardada);
    }

    /**
     * Comprueba que los identificadores solicitados no estén registrados.
     *
     * @param solicitud datos de la cuenta que se quiere crear
     * @throws CbuDuplicadoException si el CBU ya existe
     * @throws DatoCuentaDuplicadoException si el alias o número ya existe
     */
    private void validarDatosUnicos(CrearCuentaRequest solicitud) {
        if (cuentaRepository.existsByCbu(solicitud.getCbu())) {
            throw new CbuDuplicadoException(solicitud.getCbu());
        }

        if (cuentaRepository.existsByAlias(solicitud.getAlias())) {
            throw new DatoCuentaDuplicadoException(
                    "alias",
                    solicitud.getAlias()
            );
        }

        if (cuentaRepository.existsByNumeroCuenta(
                solicitud.getNumeroCuenta())) {
            throw new DatoCuentaDuplicadoException(
                    "numero de cuenta",
                    solicitud.getNumeroCuenta()
            );
        }
    }

    /**
     * Copia los datos seleccionados de una cuenta al DTO de respuesta.
     * Debe llamarse desde las operaciones transaccionales del servicio.
     *
     * @param cuenta entidad que contiene los datos de la cuenta
     * @return respuesta con los datos de la cuenta y el id del titular
     */
    private CuentaResponse convertirARespuesta(Cuenta cuenta) {

        // Para una caja de ahorro, el descubierto no corresponde.
        BigDecimal limiteDescubierto = null;

        // Solo la cuenta corriente tiene un límite de descubierto.
        if (cuenta instanceof CuentaCorriente corriente) {
            limiteDescubierto = corriente.getLimiteDescubierto();
        }

        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getTipo(),
                cuenta.getNumeroCuenta(),
                cuenta.getAlias(),
                cuenta.getCbu(),
                cuenta.getSaldo(),
                cuenta.getFechaAlta(),
                cuenta.getMoneda(),
                cuenta.getEstado(),
                cuenta.getUsuario().getId(),
                limiteDescubierto
        );
    }
}
