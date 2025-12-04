package com.example.proyecto_pixelshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

// Clase base con métodos utilitarios para todos los controladores
public abstract class BaseController {
    
    /**
     * Obtiene el email del usuario autenticado, soportando:
     * - Login tradicional (email/password)
     * - Login OAuth2 (Google)
     */
    protected String obtenerEmailDelUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            // Login tradicional
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            // Login con Google OAuth2
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                throw new IllegalStateException("No se pudo obtener el email del usuario OAuth2");
            }
            return email;
        } else if (principal instanceof String) {
            // Usuario anónimo
            return (String) principal;
        }
        
        throw new IllegalStateException("Tipo de autenticación no soportado: " + principal.getClass().getName());
    }
}
