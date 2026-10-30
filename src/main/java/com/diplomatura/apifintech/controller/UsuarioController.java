package com.diplomatura.apifintech.controller;

import com.diplomatura.apifintech.model.Usuario;
import com.diplomatura.apifintech.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Recibe solicitudes de usuarios y delega su gestión al servicio.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Creación y consulta de titulares")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * @param usuarioService servicio encargado de gestionar usuarios
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * @return lista de usuarios registrados
     */
    @Operation(summary = "Listar usuarios")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios, posiblemente vacía"
    )
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    /**
     * @param usuario datos del usuario que se desea crear
     * @return respuesta 201 con el usuario guardado y su ubicación
     */
    @Operation(
            summary = "Crear un usuario",
            description = "Recibe los datos del usuario; el id no debe enviarse."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Usuario creado",
            headers = @Header(
                    name = "Location",
                    description = "Ruta para consultar el usuario creado",
                    schema = @Schema(type = "string")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Solicitud rechazada por el servicio",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflicto con una restricción de integridad de datos",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crearUsuario(usuario);

        return ResponseEntity
                .created(URI.create("/usuarios/" + creado.getId()))
                .body(creado);
    }

    /**
     * @param id identificador del usuario buscado
     * @return datos del usuario encontrado
     */
    @Operation(summary = "Buscar un usuario por id")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(
            responseCode = "400",
            description = "El id debe ser mayor que cero",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No existe un usuario con ese id",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @GetMapping("/{id}")
    public Usuario obtenerUsuarioPorId(
            @Parameter(description = "Id del usuario", example = "1")
            @PathVariable Long id) {

        return usuarioService.obtenerUsuarioPorId(id);
    }
}
