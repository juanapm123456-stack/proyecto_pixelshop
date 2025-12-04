package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.*;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.service.interfaz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/compra")
public class CompraController extends BaseController {
    
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioJuego juegoService;
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IServicioEmail emailService;
    
    // Confirma la compra y la registra en la base de datos (DEPRECADO - usar PayPal)
    @GetMapping("/confirmar/{juegoId}")
    public String confirmarCompra(@PathVariable Integer juegoId, 
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        //  VALIDACIÓN CRÍTICA: ADMIN NO PUEDE COMPRAR
        if (usuario.getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/juego/" + juegoId;
        }
        
        Juego juego = juegoService.buscarPorId(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // Verificar que no haya comprado ya este juego
        if (compraService.usuarioComproJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/juego/" + juegoId;
        }
        
        // Crear COMPRA usando servicio (auto-crea TransaccionProveedor y TransaccionPlataforma)
        Compra compra = compraService.crear(usuario, juego, juego.getPrecio(), "Pago Online", null);
        compraService.completar(compra.getId());
        
        // Enviar email de confirmación
        try {
            System.out.println(" Intentando enviar email de confirmación a: " + usuario.getEmail());
            emailService.enviarConfirmacionCompra(compra);
            System.out.println(" Email enviado correctamente");
        } catch (Exception e) {
            System.err.println(" Error al enviar email de confirmación: " + e.getMessage());
            e.printStackTrace();
        }
        
        redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
        return "redirect:/mi-biblioteca";
    }
    
    @PostMapping("/{juegoId}")
    public String comprarJuego(@PathVariable Integer juegoId, 
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        // Verificar que el usuario esté autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para comprar");
            return "redirect:/login";
        }
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que no sea ADMIN
        if (usuario.getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/juego/" + juegoId;
        }
        
        // Verificar que el juego existe
        Juego juego = juegoService.buscarPorId(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // Verificar que no lo haya comprado ya
        if (compraService.usuarioComproJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/mi-biblioteca";
        }
        
        // Redirigir al checkout para seleccionar método de pago
        return "redirect:/paypal/checkout/" + juegoId;
    }
    
    @GetMapping("/mis-compras")
    public String misCompras(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Compra> compras = compraService.listarPorUsuario(usuario);
        model.addAttribute("compras", compras);
        
        return "usuario/mis-compras";
    }
}
