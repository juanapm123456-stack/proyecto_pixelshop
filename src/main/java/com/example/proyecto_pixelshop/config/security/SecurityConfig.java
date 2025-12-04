package com.example.proyecto_pixelshop.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Configuración de seguridad de Spring Security
// Define reglas de autorización por URL, configura login local (email/password) y login con Google OAuth2
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private GoogleOAuth2UserService googleOAuth2UserService;
     
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/", "/juego/**", "/buscar").authenticated()
                //  CRÍTICO: Solo CLIENTE y PROVEEDOR pueden comprar
                .requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")
                .requestMatchers("/mi-biblioteca").hasAnyRole("CLIENTE", "PROVEEDOR")
                .requestMatchers("/perfil").authenticated()
                .requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(googleOAuth2UserService)
                )
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Solo para desarrollo, ACTÍVALO en producción
        
        return http.build();
    }
    
    // Bean para permitir autenticación programática (usado en el registro automático)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
