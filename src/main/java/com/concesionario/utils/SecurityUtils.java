package com.concesionario.utils;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.security.Principal;

public class SecurityUtils {

    public static String getEmailFromPrincipal(Principal principal) {
        if (principal == null) return null;

        if (principal instanceof OAuth2AuthenticationToken token) {
            Object principalObj = token.getPrincipal();
            if (principalObj instanceof OidcUser oidcUser) {
                return oidcUser.getEmail();
            } else if (principalObj instanceof OAuth2User oauth2User) {
                return oauth2User.getAttribute("email");
            }
        }
        
        // Login tradicional o fallback
        return principal.getName();
    }
}
