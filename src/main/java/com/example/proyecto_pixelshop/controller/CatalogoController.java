package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.service.interfaz.IServicioJuego;
import com.example.proyecto_pixelshop.service.interfaz.IServicioCompra;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CatalogoController extends BaseController {
    
    @Autowired private IServicioJuego juegoService;
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioUsuario usuarioService;
    
    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        List<Juego> juegos = juegoService.listarActivos();
        model.addAttribute("juegos", juegos);
        
        // Si hay usuario logueado, obtener sus juegos comprados (solo CLIENTE y PROVEEDOR)
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
            if (usuario != null && usuario.getRol() != Rol.ADMIN) {
                List<Compra> compras = compraService.listarPorUsuario(usuario);
                
                // Obtener IDs de juegos comprados
                List<Integer> juegosCompradosIds = compras.stream()
                    .map(c -> c.getJuego().getId())
                    .collect(Collectors.toList());
                
                // Obtener NOMBRES de juegos comprados (para comparar por nombre)
                List<String> juegosCompradosNombres = compras.stream()
                    .map(c -> c.getJuego().getTitulo().toLowerCase())
                    .collect(Collectors.toList());
                
                model.addAttribute("juegosComprados", juegosCompradosIds);
                model.addAttribute("juegosCompradosNombres", juegosCompradosNombres);
            }
        }
        
        return "catalogo/index";
    }
    
    @GetMapping("/juego/{id}")
    public String detalleJuego(@PathVariable Integer id, Model model, 
                               Authentication authentication) {
        Juego juego = juegoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        model.addAttribute("juego", juego);
        
        // Verificar si el usuario ya compró este juego O un juego con el mismo nombre (solo para CLIENTE y PROVEEDOR)
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
            if (usuario != null) {
                // Los ADMIN nunca pueden "haber comprado" porque no pueden comprar
                boolean yaComprado = false;
                if (usuario.getRol() != Rol.ADMIN) {
                    // Verificar si compró este juego específico O un juego con el mismo nombre
                    List<Compra> compras = compraService.listarPorUsuario(usuario);
                    yaComprado = compras.stream()
                        .anyMatch(c -> c.getJuego().getId().equals(juego.getId()) || 
                                      c.getJuego().getTitulo().equalsIgnoreCase(juego.getTitulo()));
                }
                model.addAttribute("yaComprado", yaComprado);
            }
        }
        
        return "catalogo/juego-detalle";
    }
    
    @GetMapping("/buscar")
    public String buscar(@RequestParam String q, Model model, Authentication authentication) {
        List<Juego> juegos = juegoService.buscarPorTitulo(q);
        model.addAttribute("juegos", juegos);
        model.addAttribute("busqueda", q);
        
        // Si hay usuario logueado, obtener sus juegos comprados (solo CLIENTE y PROVEEDOR)
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
            if (usuario != null && usuario.getRol() != Rol.ADMIN) {
                List<Compra> compras = compraService.listarPorUsuario(usuario);
                
                // Obtener IDs de juegos comprados
                List<Integer> juegosCompradosIds = compras.stream()
                    .map(c -> c.getJuego().getId())
                    .collect(Collectors.toList());
                
                // Obtener NOMBRES de juegos comprados (para comparar por nombre)
                List<String> juegosCompradosNombres = compras.stream()
                    .map(c -> c.getJuego().getTitulo().toLowerCase())
                    .collect(Collectors.toList());
                
                model.addAttribute("juegosComprados", juegosCompradosIds);
                model.addAttribute("juegosCompradosNombres", juegosCompradosNombres);
            }
        }
        
        return "catalogo/index";
    }
}
