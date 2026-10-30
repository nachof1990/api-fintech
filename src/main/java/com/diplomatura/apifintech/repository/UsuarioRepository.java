package com.diplomatura.apifintech.repository;


import com.diplomatura.apifintech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
