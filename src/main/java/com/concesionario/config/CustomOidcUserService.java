package com.concesionario.config;

import com.concesionario.model.Rol;
import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private com.concesionario.repository.AdministradorRepository administradorRepository;

    @Autowired
    private com.concesionario.repository.TrabajadorRepository trabajadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("given_name");
        String lastName = oidcUser.getAttribute("family_name");
        String sub = oidcUser.getAttribute("sub"); // Google ID

        System.out.println("DEBUG OIDC: Processing login for email: " + email);

        // 1. Check if user is an Administrator
        Optional<com.concesionario.model.Administrador> adminOptional = administradorRepository.findByCorreoAdmin(email);
        if (adminOptional.isPresent()) {
            System.out.println("DEBUG OIDC: User found as Administrator");
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
            // Return DefaultOidcUser to keep OIDC functionality (ID token, etc)
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        }

        // 2. Check if user is a Worker
        Optional<com.concesionario.model.Trabajador> trabajadorOptional = trabajadorRepository.findByCorreo(email);
        if (trabajadorOptional.isPresent()) {
            System.out.println("DEBUG OIDC: User found as Worker");
            Set<GrantedAuthority> authorities = new HashSet<>();
            for (Rol rol : trabajadorOptional.get().getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
            }
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        }

        // 3. Regular User Logic (Create or Update)
        Optional<Usuario> userOptional = usuarioRepository.findByCorreoUser(email);

        Usuario usuario;
        if (userOptional.isPresent()) {
            System.out.println("DEBUG OIDC: Updating existing Usuario");
            usuario = userOptional.get();
            usuario.setNombreUser(name != null ? name : usuario.getNombreUser());
            usuario.setApellidoUser(lastName != null ? lastName : usuario.getApellidoUser());
            usuarioRepository.save(usuario);
        } else {
            System.out.println("DEBUG OIDC: Creating NEW Usuario");
            // Register new user
            usuario = new Usuario();
            usuario.setCorreoUser(email);
            usuario.setNombreUser(name != null ? name : "Usuario");
            usuario.setApellidoUser(lastName != null ? lastName : "Google");
            usuario.setIdentificacionUser(sub); // Use Google ID as identification initially
            usuario.setPasswordUser(passwordEncoder.encode("SOCIAL_LOGIN_nopassword"));
            usuario.setRol(Rol.USUARIO);
            usuario.setFechaCreacion(LocalDateTime.now());

            Usuario saved = usuarioRepository.save(usuario);
            System.out.println("DEBUG OIDC: Saved new user with ID: " + saved.getId());
        }

        // Mapear el rol del usuario a GrantedAuthority
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
