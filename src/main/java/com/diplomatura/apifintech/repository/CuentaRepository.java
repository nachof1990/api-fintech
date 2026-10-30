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

    /**
     * Busca el alias excluyendo la cuenta que estamos actualizando.
     *
     * @param alias alias solicitado
     * @param id identificador de la cuenta que debe excluirse
     * @return true si otra cuenta tiene ese alias
     */
    boolean existsByAliasAndIdNot(String alias, Long id);
}