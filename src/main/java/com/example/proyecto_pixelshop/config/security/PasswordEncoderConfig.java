package com.example.proyecto_pixelshop.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Esta clase se encarga de proporcionar el codificador de contraseñas BCrypt.
 * BCrypt es un algoritmo de hash seguro que se usa para encriptar las contraseñas
 * antes de guardarlas en la base de datos.
 */
@Configuration
public class PasswordEncoderConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
