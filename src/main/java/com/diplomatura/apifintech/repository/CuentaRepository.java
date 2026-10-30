package com.diplomatura.apifintech.repository;

import com.diplomatura.apifintech.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permite guardar y consultar cuentas de ambos tipos.
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    /**
     * @param cbu CBU que se desea buscar
     * @return true si alguna cuenta tiene ese CBU
     */
    boolean existsByCbu(String cbu);

    /**
     * @param alias alias que se desea buscar
     * @return true si alguna cuenta tiene ese alias
     */
    boolean existsByAlias(String alias);

    /**
     * @param numeroCuenta número que se desea buscar
     * @return true si alguna cuenta tiene ese número
     */
    boolean existsByNumeroCuenta(String numeroCuenta);
}