package com.concesionario.config;

import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String email = oauthToken.getPrincipal().getAttribute("email");
        String googleId = oauthToken.getPrincipal().getAttribute("sub");

        Optional<Usuario> usuarioOptional = usuarioRepository.findByCorreoUser(email);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            // Check if identification is the Google ID (placeholder) or empty
            if (usuario.getIdentificacionUser() == null || usuario.getIdentificacionUser().equals(googleId)) {
                // Redirect to profile completion page
                getRedirectStrategy().sendRedirect(request, response, "/usuario/completar-registro");
            } else {
                // Valid profile, go to home or strictly perfil if requested
                getRedirectStrategy().sendRedirect(request, response, "/usuario/Inicio");
            }
        } else {
            // Should not happen if CustomOAuth2UserService works, but fallback
            getRedirectStrategy().sendRedirect(request, response, "/usuario/Inicio");
        }
    }
}
