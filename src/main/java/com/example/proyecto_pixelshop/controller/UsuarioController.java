package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.service.interfaz.IServicioCompra;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import com.example.proyecto_pixelshop.service.interfaz.IAzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.net.URL;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;

@Controller
public class UsuarioController extends BaseController {
    
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IAzureBlobStorageService azureStorageService;
    
    @GetMapping("/mi-biblioteca")
    public String miBiblioteca(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Compra> biblioteca = compraService.obtenerBiblioteca(usuario);
        model.addAttribute("biblioteca", biblioteca);
        
        return "usuario/mi-biblioteca";
    }
    
    @GetMapping("/descargar-juego/{juegoId}")
    public ResponseEntity<?> descargarJuego(@PathVariable Integer juegoId, 
                                             Authentication authentication) {
        try {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Verificar que el usuario ha comprado el juego
            boolean haComprado = compraService.obtenerBiblioteca(usuario).stream()
                .anyMatch(compra -> compra.getJuego().getId().equals(juegoId));
            
            if (!haComprado) {
                return ResponseEntity.status(403)
                    .body("No tienes permiso para descargar este juego");
            }
            
            // Obtener el juego
            Juego juego = compraService.obtenerBiblioteca(usuario).stream()
                .filter(c -> c.getJuego().getId().equals(juegoId))
                .map(Compra::getJuego)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
            
            // Verificar que el juego tiene archivo
            if (juego.getArchivoDescargableUrl() == null || juego.getArchivoDescargableUrl().isEmpty()) {
                return ResponseEntity.status(404)
                    .body("Este juego no tiene archivo disponible para descarga");
            }
            
            // Descargar archivo desde Azure
            URL url = new URL(juego.getArchivoDescargableUrl());
            InputStream inputStream = url.openStream();
            
            // Preparar respuesta con el archivo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", juego.getArchivoNombre());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body("Error al descargar el archivo: " + e.getMessage());
        }
    }
}
