package com.diplomatura.apifintech.service;

import com.diplomatura.apifintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import com.diplomatura.apifintech.model.Usuario;
import com.diplomatura.apifintech.exception.UsuarioNoEncontradoException;
import java.util.List;

@Service
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;


    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    public Usuario crearUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }


        return usuarioRepository.save(usuario);
    }
    public Usuario obtenerUsuarioPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El id del usuario debe ser mayor que cero"
            );
        }


        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
