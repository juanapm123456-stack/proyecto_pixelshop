package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import com.example.proyecto_pixelshop.service.interfaz.IServicioEmail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IServicioEmail emailService;
    @Autowired private AuthenticationManager authenticationManager;
    
    // Repositorio para guardar el contexto de seguridad en la sesión HTTP
    private final SecurityContextRepository securityContextRepository = 
        new HttpSessionSecurityContextRepository();
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registrar(@ModelAttribute Usuario usuario, 
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        // Verificar si email ya existe
        if (usuarioService.existeEmail(usuario.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
            return "redirect:/register";
        }
        
        // Si no viene rol del formulario, asignar CLIENTE por defecto
        if (usuario.getRol() == null) {
            usuario.setRol(Rol.CLIENTE);
        }
        usuario.setActivo(true);
        
        // Guardar la contraseña sin encriptar temporalmente (necesaria para autenticación)
        String passwordSinEncriptar = usuario.getPassword();
        
        // Registrar el usuario (esto encripta la contraseña automáticamente)
        Usuario usuarioRegistrado = usuarioService.registrar(usuario);
        
        // Enviar email de bienvenida
        try {
            System.out.println("📧 Intentando enviar email de bienvenida a: " + usuario.getEmail());
            emailService.enviarEmailBienvenida(usuario);
            System.out.println("✅ Email de bienvenida enviado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Autenticar automáticamente al usuario después del registro
        try {
            // Crear token de autenticación con email y contraseña sin encriptar
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(usuarioRegistrado.getEmail(), passwordSinEncriptar);
            
            // Autenticar usando el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // Crear contexto de seguridad y establecer la autenticación
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Guardar el contexto de seguridad en la sesión HTTP
            securityContextRepository.saveContext(securityContext, request, response);
            
            System.out.println("✅ Usuario autenticado automáticamente después del registro: " + usuarioRegistrado.getEmail());
            
            // Redirigir al catálogo con mensaje de bienvenida
            redirectAttributes.addFlashAttribute("success", "¡Bienvenido a PixelShop! Tu cuenta ha sido creada exitosamente");
            return "redirect:/";
            
        } catch (Exception e) {
            System.err.println("❌ Error al autenticar usuario después del registro: " + e.getMessage());
            e.printStackTrace();
            // Si falla la autenticación automática, redirigir al login
            redirectAttributes.addFlashAttribute("error", "Registro exitoso pero hubo un error al iniciar sesión. Por favor, inicia sesión manualmente.");
            return "redirect:/login";
        }
    }
}
