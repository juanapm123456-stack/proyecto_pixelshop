package com.example.proyecto_pixelshop.config.security;

import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.repository.UsuarioRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

// Servicio de autenticación con Google OAuth2
// Gestiona el inicio de sesión con Google: obtiene datos del usuario, lo registra si es nuevo y asigna rol CLIENTE
@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private IServicioEmail emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Obtener información del usuario de Google
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Extraer datos del usuario
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String nombre = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");

        // Buscar usuario ACTIVO por email
        Usuario usuario = usuarioRepository.findByEmailAndActivo(email, true).orElse(null);

        if (usuario == null) {
            // No existe usuario activo, crear nuevo usuario
            String randomPassword = UUID.randomUUID().toString();
            
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombre(nombre);
            usuario.setPassword(passwordEncoder.encode(randomPassword));
            usuario.setRol(Rol.CLIENTE);
            usuario.setIdGoogle(googleId);
            usuario.setActivo(true);
            
            usuarioRepository.save(usuario);
            
            // Enviar email de bienvenida
            try {
                System.out.println("📧 [OAuth2] Intentando enviar email de bienvenida a: " + usuario.getEmail());
                emailService.enviarEmailBienvenida(usuario);
                System.out.println("✅ [OAuth2] Email de bienvenida enviado correctamente");
            } catch (Exception e) {
                System.err.println("❌ [OAuth2] Error al enviar email de bienvenida: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("✅ Nuevo usuario creado vía Google OAuth2: " + email + " (ID: " + usuario.getId() + ")");
        } else {
            // Usuario existe y está activo
            System.out.println("✅ Usuario existente iniciado sesión vía Google OAuth2: " + email + " (ID: " + usuario.getId() + ")");
        }

        // Retornar usuario con rol asignado
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())),
            attributes,
            "email"
        );
    }
}
