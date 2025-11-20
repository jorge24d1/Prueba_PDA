package com.concesionario.config;

import com.concesionario.service.AuthService;
import com.concesionario.service.TrabajadorDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthService authService;

    @Autowired
    private TrabajadorDetailsService trabajadorDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/usuario/Inicio",
                                "/ubicaciones",
                                "/terminos",
                                "/cookies",
                                "/registro-admin",

                                "/usuario",
                                "/registro",
                                "/vehiculos"
                                ,"/vehiculos/explorar/{id}",
                                "/styles4.css",
                                "/images/**",
                                "/nosotros",
                                "/garantias",
                                "/credito",
                                "/api/chatbot/mensaje",


                                "/login",
                                "/usuario/agendamiento",
                                "/usuario/loginup",
                                "/css/**",
                                "/js/**",
                                "/STloginup.css",
                                "/uploads/**",
                                "/auth/**"

                        ).permitAll()
                        // ✅ NUEVO: Protección para roles específicos de trabajadores
                        .requestMatchers("/perfil_gestor").hasRole("TRB_GESTOR")
                        .requestMatchers("/perfil_analisis").hasRole("TRB_ANALISIS")
                        .requestMatchers("/perfil_asesor").hasRole("TRB_ASESOR")
                        .requestMatchers("/admin/**").hasAnyRole("ADMINISTRADOR", "TRABAJADOR", "TRB_GESTOR", "TRB_ANALISIS", "TRB_ASESOR")
                        .requestMatchers("/usuario/cita", "/usuario/cita/guardar").hasAnyRole("USUARIO", "ADMINISTRADOR", "TRABAJADOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/usuario/Inicio")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsService());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                return authService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                return trabajadorDetailsService.loadUserByUsername(username);
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // ✅ NUEVA LÓGICA: Redirección inteligente por roles
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRB_GESTOR"))) {
                response.sendRedirect("/perfil_gestor");
            }
            else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRB_ANALISIS"))) {
                response.sendRedirect("/perfil_analisis");
            }
            else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRB_ASESOR"))) {
                response.sendRedirect("/perfil_asesor");
            }
            else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
                response.sendRedirect("/admin/Dashboard");
            }
            else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRABAJADOR"))) {
                response.sendRedirect("/trabajador/dashboard");
            }
            else {
                response.sendRedirect("/usuario/Inicio");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}