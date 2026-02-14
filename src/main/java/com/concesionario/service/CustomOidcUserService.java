package com.concesionario.service;

import com.concesionario.model.Administrador;
import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.repository.AdministradorRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        
        logger.info("Cargando usuario OIDC para el correo: {}", email);
        System.out.println("DEBUG OIDC: Cargando usuario OIDC para: " + email);

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        if (email != null) {
            // 1. Verificar si es Administrador
            Optional<Administrador> adminOpt = administradorRepository.findByCorreoAdmin(email);
            if (adminOpt.isPresent()) {
                logger.info("Usuario OIDC identificado como ADMINISTRADOR: {}", email);
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
            } 
            // 2. Verificar si es Trabajador
            else {
                Optional<Trabajador> trabajadorOpt = trabajadorRepository.findByCorreo(email);
                if (trabajadorOpt.isPresent()) {
                    logger.info("Usuario OIDC identificado como TRABAJADOR: {}", email);
                    trabajadorOpt.get().getRoles().forEach(rol -> {
                        logger.info("Añadiendo rol OIDC de trabajador: ROLE_{}", rol.name());
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
                    });
                    authorities.add(new SimpleGrantedAuthority("ROLE_TRABAJADOR"));
                } 
                // 3. Verificar si es Usuario existente o crear nuevo
                else {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoUser(email);
                    Usuario usuario;
                    if (usuarioOpt.isPresent()) {
                        logger.info("Usuario OIDC identificado como USUARIO existente: {}", email);
                        usuario = usuarioOpt.get();
                    } else {
                        logger.info("Creando nuevo USUARIO OIDC para Google: {}", email);
                        usuario = new Usuario();
                        usuario.setCorreoUser(email);
                        usuario.setNombreUser(oidcUser.getGivenName());
                        usuario.setApellidoUser(oidcUser.getFamilyName());
                        usuario.setRol(Rol.USUARIO);
                        usuario.setFechaCreacion(LocalDateTime.now());
                        usuarioRepository.save(usuario);
                    }
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                }
            }
        }

        logger.info("Autoridades finales OIDC asignadas: {}", authorities);
        System.out.println("DEBUG OIDC: Autoridades finales asignadas: " + authorities);

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
