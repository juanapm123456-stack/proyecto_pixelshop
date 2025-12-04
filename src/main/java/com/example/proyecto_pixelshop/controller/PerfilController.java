package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.service.interfaz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController extends BaseController {
    
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioTransaccionProveedor transaccionProveedorService;
    @Autowired private IServicioJuego juegoService;
    
    @GetMapping
    public String mostrarPerfil(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "usuario/perfil";
    }
    
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setNombre(usuarioActualizado.getNombre());
        
        // Si es proveedor, permitir actualizar cifNif y email PayPal
        if (usuario.getRol() == Rol.PROVEEDOR) {
            if (usuarioActualizado.getCifNif() != null) {
                usuario.setCifNif(usuarioActualizado.getCifNif());
            }
            if (usuarioActualizado.getEmailPaypal() != null) {
                usuario.setEmailPaypal(usuarioActualizado.getEmailPaypal());
            }
        }
        
        usuarioService.actualizar(usuario);
        
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }
    
    @DeleteMapping("/eliminar")
    @ResponseBody
    public String eliminarCuenta(Authentication authentication) {
        try {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            System.out.println("🗑️ Eliminando cuenta del usuario: " + usuario.getEmail());
            
            // El servicio maneja las eliminaciones en cascada automáticamente (cascade = ALL)
            usuarioService.eliminar(usuario.getId());
            System.out.println("✅ Usuario eliminado correctamente");
            
            return "OK";
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar cuenta: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar cuenta: " + e.getMessage());
        }
    }
}
