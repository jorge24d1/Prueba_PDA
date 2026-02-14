package com.concesionario.service;

import com.concesionario.model.Administrador;
import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.repository.AdministradorRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        logger.info("Cargando usuario OAuth2 para el correo: {}", email);
        System.out.println("DEBUG: Cargando usuario OAuth2 para: " + email);
        
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());

        if (email != null) {
            // 1. Verificar si es Administrador
            Optional<Administrador> adminOpt = administradorRepository.findByCorreoAdmin(email);
            if (adminOpt.isPresent()) {
                logger.info("Usuario identificado como ADMINISTRADOR: {}", email);
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
            } 
            // 2. Verificar si es Trabajador
            else {
                Optional<Trabajador> trabajadorOpt = trabajadorRepository.findByCorreo(email);
                if (trabajadorOpt.isPresent()) {
                    logger.info("Usuario identificado como TRABAJADOR: {}", email);
                    trabajadorOpt.get().getRoles().forEach(rol -> {
                        logger.info("Añadiendo rol de trabajador: ROLE_{}", rol.name());
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
                    });
                    // Asegurar el rol base TRABAJADOR
                    authorities.add(new SimpleGrantedAuthority("ROLE_TRABAJADOR"));
                } 
                // 3. Verificar si es Usuario existente o crear nuevo
                else {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoUser(email);
                    Usuario usuario;
                    if (usuarioOpt.isPresent()) {
                        logger.info("Usuario identificado como USUARIO existente: {}", email);
                        usuario = usuarioOpt.get();
                    } else {
                        logger.info("Creando nuevo USUARIO para Google: {}", email);
                        usuario = new Usuario();
                        usuario.setCorreoUser(email);
                        usuario.setNombreUser(givenName);
                        usuario.setApellidoUser(familyName);
                        usuario.setRol(Rol.USUARIO);
                        usuario.setFechaCreacion(LocalDateTime.now());
                        usuarioRepository.save(usuario);
                    }
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                }
            }
        }

        logger.info("Autoridades finales asignadas: {}", authorities);
        System.out.println("DEBUG: Autoridades finales asignadas: " + authorities);

        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}
