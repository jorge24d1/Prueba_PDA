package com.concesionario.service;

import com.concesionario.model.Rol;
import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private EmailService EmailService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // MÉTODOS EXISTENTES
    public void registrarUsuario(String nombre, String apellido,
                                 String email, String identificacion,
                                 String password, Rol rol) {

        String passwordEncriptado = passwordEncoder.encode(password);

        Usuario usuario = new Usuario();
        usuario.setNombreUser(nombre);
        usuario.setApellidoUser(apellido);
        usuario.setCorreoUser(email);
        usuario.setIdentificacionUser(identificacion);
        usuario.setPasswordUser(passwordEncriptado);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setRol(rol);

        EmailService.enviarCorreoBienvenida(email, nombre, apellido);

        usuarioRepository.save(usuario);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public boolean existeCorreoEnCualquierTabla(String correo) {
        return usuarioRepository.existsByCorreoUser(correo);
    }

    public boolean existeIdentificacionEnCualquierTabla(String identificacion) {
        return usuarioRepository.existsByIdentificacionUser(identificacion);
    }

    public Usuario findByCorreoUser(String correo) {
        return usuarioRepository.findByCorreoUser(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));
    }

    // NUEVOS MÉTODOS NECESARIOS PARA PREDICCIÓN
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(String id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByResetPasswordToken(String token) {
        return usuarioRepository.findByResetPasswordToken(token);
    }
}