package com.concesionario.config;

import com.concesionario.model.Rol;
import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    @Autowired
    private com.concesionario.repository.AdministradorRepository administradorRepository;

    @Autowired
    private com.concesionario.repository.TrabajadorRepository trabajadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String sub = oAuth2User.getAttribute("sub"); // Google ID
        System.out.println("DEBUG: Processing OAuth2 login for email: " + email);

        // 1. Check if user is an Administrator
        Optional<com.concesionario.model.Administrador> adminOptional = administradorRepository.findByCorreoAdmin(email);
        if (adminOptional.isPresent()) {
            System.out.println("DEBUG: User found as Administrator");
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
            return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
        }

        // 2. Check if user is a Worker
        Optional<com.concesionario.model.Trabajador> trabajadorOptional = trabajadorRepository.findByCorreo(email);
        if (trabajadorOptional.isPresent()) {
            System.out.println("DEBUG: User found as Worker");
            Set<GrantedAuthority> authorities = new HashSet<>();
            for (Rol rol : trabajadorOptional.get().getRoles()) {
                 authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
            }
            return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
        }

        // 3. Regular User Logic (Create or Update)
        Optional<Usuario> userOptional = usuarioRepository.findByCorreoUser(email);

        Usuario usuario;
        if (userOptional.isPresent()) {
            System.out.println("DEBUG: Updating existing Usuario");
            usuario = userOptional.get();
             usuario.setNombreUser(name != null ? name : usuario.getNombreUser());
             usuario.setApellidoUser(lastName != null ? lastName : usuario.getApellidoUser());
             usuarioRepository.save(usuario);
        } else {
            System.out.println("DEBUG: Creating NEW Usuario");
            // Register new user
            usuario = new Usuario();
            usuario.setCorreoUser(email);
            usuario.setNombreUser(name != null ? name : "Usuario");
            usuario.setApellidoUser(lastName != null ? lastName : "Google");
            usuario.setIdentificacionUser(sub); // Use Google ID as identification
            usuario.setPasswordUser(passwordEncoder.encode("SOCIAL_LOGIN_nopassword"));
            usuario.setRol(Rol.USUARIO);
            usuario.setFechaCreacion(LocalDateTime.now());
            
            Usuario savedUser = usuarioRepository.save(usuario);
            System.out.println("DEBUG: Saved new user with ID: " + savedUser.getId());
        }

        // Mapear el rol del usuario a GrantedAuthority
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email" // El atributo que se usará como "nombre" principal
        );
    }
}
