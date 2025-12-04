package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.*;
import com.example.proyecto_pixelshop.model.enums.EstadoPago;
import com.example.proyecto_pixelshop.repository.*;
import com.example.proyecto_pixelshop.service.interfaz.IAzureBlobStorageService;
import com.example.proyecto_pixelshop.service.interfaz.IServicioJuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class ProveedorController extends BaseController {
    
    @Autowired private JuegoRepository juegoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransaccionProveedorRepository transaccionProveedorRepository;
    @Autowired private TransaccionPlataformaRepository transaccionPlataformaRepository;
    @Autowired private IAzureBlobStorageService azureStorageService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private IServicioJuego juegoService;
    
    @GetMapping("/publicar")
    public String mostrarFormularioPublicar(Model model) {
        model.addAttribute("juego", new Juego());
        return "proveedor/publicar-juego";
    }
    
    @PostMapping("/publicar")
    public String publicarJuego(@RequestParam String titulo,
                                @RequestParam Double precio,
                                @RequestParam String descripcion,
                                @RequestParam(required = false) String genero,
                                @RequestParam(required = false) String videoUrl,
                                @RequestParam("imagen1") MultipartFile imagen1,
                                @RequestParam("imagen2") MultipartFile imagen2,
                                @RequestParam("imagen3") MultipartFile imagen3,
                                @RequestParam("imagen4") MultipartFile imagen4,
                                @RequestParam(value = "archivoJuego", required = false) MultipartFile archivoJuego,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario proveedor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            // Validar y subir imágenes a Azure
            String url1 = null, url2 = null, url3 = null, url4 = null;
            
            // Guardar imagen 1
            if (!imagen1.isEmpty()) {
                String validationResult = validateImage(imagen1, "Imagen 1");
                if (validationResult != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult);
                    return "redirect:/proveedor/publicar";
                }
                url1 = azureStorageService.uploadFile(imagen1, "juegos");
            }
            
            // Guardar imagen 2
            if (!imagen2.isEmpty()) {
                String validationResult2 = validateImage(imagen2, "Imagen 2");
                if (validationResult2 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult2);
                    return "redirect:/proveedor/publicar";
                }
                url2 = azureStorageService.uploadFile(imagen2, "juegos");
            }
            
            // Guardar imagen 3
            if (!imagen3.isEmpty()) {
                String validationResult3 = validateImage(imagen3, "Imagen 3");
                if (validationResult3 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult3);
                    return "redirect:/proveedor/publicar";
                }
                url3 = azureStorageService.uploadFile(imagen3, "juegos");
            }
            
            // Guardar imagen 4
            if (!imagen4.isEmpty()) {
                String validationResult4 = validateImage(imagen4, "Imagen 4");
                if (validationResult4 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult4);
                    return "redirect:/proveedor/publicar";
                }
                url4 = azureStorageService.uploadFile(imagen4, "juegos");
            }
            
            // Validar que se hayan subido las 4 imágenes
            if (url1 == null || url2 == null || url3 == null || url4 == null) {
                redirectAttributes.addFlashAttribute("error", "Debes subir las 4 imágenes obligatorias (1 portada + 3 capturas)");
                return "redirect:/proveedor/publicar";
            }
            
            // Subir archivo del juego (ZIP) si se proporcionó
            String archivoUrl = null;
            String archivoNombre = null;
            Integer archivoTamanio = null;
            
            if (archivoJuego != null && !archivoJuego.isEmpty()) {
                // Validar tamaño (500MB máximo)
                long maxSize = 500L * 1024 * 1024; // 500MB
                if (archivoJuego.getSize() > maxSize) {
                    redirectAttributes.addFlashAttribute("error", "El archivo del juego es demasiado grande. Máximo: 500MB");
                    return "redirect:/proveedor/publicar";
                }
                
                // Validar extensión
                String fileName = archivoJuego.getOriginalFilename();
                if (fileName == null || !fileName.matches(".*\\.(zip|rar|7z)$")) {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser .zip, .rar o .7z");
                    return "redirect:/proveedor/publicar";
                }
                
                // Subir a Azure Blob Storage
                archivoUrl = azureStorageService.uploadFile(archivoJuego, "juegos-archivos");
                archivoNombre = archivoJuego.getOriginalFilename();
                archivoTamanio = (int) (archivoJuego.getSize() / (1024 * 1024)); // Convertir a MB
                
                System.out.println("📦 Archivo subido: " + archivoNombre + " (" + archivoTamanio + " MB)");
            }
            
            // Crear el juego
            Juego juego = new Juego();
            juego.setTitulo(titulo);
            juego.setPrecio(precio);
            juego.setDescripcion(descripcion);
            juego.setGenero(genero);
            juego.setVideoPromocionalUrl(videoUrl);
            juego.setImagenPortadaUrl(url1);
            juego.setImagen2Url(url2);
            juego.setImagen3Url(url3);
            juego.setImagen4Url(url4);
            juego.setArchivoDescargableUrl(archivoUrl);
            juego.setArchivoNombre(archivoNombre);
            juego.setArchivoTamanioBytes(archivoTamanio);
            juego.setProveedor(proveedor);
            juego.setMontoPublicacion(25.00);
            
            // Publicar usando el servicio (incluye validación de nombre único)
            juegoService.publicar(juego);
            
            // La transacción ya se crea automáticamente en juegoService.publicar()
            
            redirectAttributes.addFlashAttribute("success", "¡Juego publicado exitosamente!");
            return "redirect:/proveedor/mis-juegos";
            
        } catch (RuntimeException e) {
            // Capturar error de validación de nombre duplicado
            if (e.getMessage().contains("Ya existe un juego activo con el nombre")) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/proveedor/publicar";
            }
            // Re-lanzar otras excepciones
            throw e;
        } catch (IOException e) {
            String errorMessage = "Error al procesar las imágenes. ";
            if (e.getMessage().contains("No space left")) {
                errorMessage += "No hay espacio suficiente en el servidor.";
            } else if (e.getMessage().contains("Access denied") || e.getMessage().contains("Permission")) {
                errorMessage += "No se tienen permisos para guardar archivos.";
            } else if (e.getMessage().contains("File too large")) {
                errorMessage += "Una o más imágenes superan el tamaño máximo de 10MB.";
            } else {
                errorMessage += "Verifica que las imágenes sean válidas (JPG, PNG, WEBP) y no superen 10MB cada una.";
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/proveedor/publicar";
        }
    }
    
    @GetMapping("/mis-juegos")
    public String misJuegos(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario proveedor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Juego> juegos = juegoRepository.findByProveedor(proveedor);
        model.addAttribute("juegos", juegos);
        
        return "proveedor/mis-juegos";
    }
    
    @GetMapping("/ventas")
    public String ventas(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario proveedor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Obtener todos los movimientos del proveedor
        List<TransaccionProveedor> movimientos = transaccionProveedorRepository
            .findByUsuarioOrderByFechaVentaDesc(proveedor);
        
        // Calcular ingresos pendientes (85% de las ventas no cobradas)
        double ingresosPendientes = movimientos.stream()
            .filter(t -> t.getEstadoPago() == EstadoPago.PENDIENTE)
            .mapToDouble(TransaccionProveedor::getImporteNeto)
            .sum();
        
        model.addAttribute("usuario", proveedor);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("ingresosPendientes", String.format("%.2f", ingresosPendientes));
        
        return "proveedor/ventas";
    }
    
    @PostMapping("/cobrar/{movimientoId}")
    public String cobrarMovimiento(@PathVariable Integer movimientoId,
                                   @RequestParam String metodoCobro,
                                   @RequestParam(required = false) String emailPaypal,
                                   @RequestParam(required = false) String numeroTarjeta,
                                   @RequestParam(required = false) String titularTarjeta,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario proveedor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Buscar la transacción
            TransaccionProveedor transaccion = transaccionProveedorRepository.findById(movimientoId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
            
            // Validar que la transacción pertenece al proveedor
            // Obtener proveedor desde compra.juego.proveedor
            if (!transaccion.getCompra().getJuego().getProveedor().getId().equals(proveedor.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cobrar esta transacción");
                return "redirect:/proveedor/ventas";
            }
            
            // Validar que está pendiente
            if (transaccion.getEstadoPago() != EstadoPago.PENDIENTE) {
                redirectAttributes.addFlashAttribute("error", "Esta transacción ya fue cobrada");
                return "redirect:/proveedor/ventas";
            }
            
            // Validar método de cobro
            String metodoPago = "";
            if ("paypal".equals(metodoCobro)) {
                if (emailPaypal == null || emailPaypal.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debes proporcionar un email de PayPal");
                    return "redirect:/proveedor/ventas";
                }
                metodoPago = "PayPal (" + emailPaypal + ")";
            } else if ("tarjeta".equals(metodoCobro)) {
                if (numeroTarjeta == null || numeroTarjeta.isEmpty() || titularTarjeta == null || titularTarjeta.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debes proporcionar los datos de la tarjeta");
                    return "redirect:/proveedor/ventas";
                }
                // Ocultar parte del IBAN por seguridad
                String ibanOculto = "****" + numeroTarjeta.substring(Math.max(0, numeroTarjeta.length() - 4));
                metodoPago = "Transferencia bancaria (" + ibanOculto + " - " + titularTarjeta + ")";
            }
            
            // Marcar como pagado
            transaccion.setEstadoPago(EstadoPago.PAGADO);
            transaccion.setFechaPago(java.time.LocalDateTime.now());
            transaccionProveedorRepository.save(transaccion);
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("¡Cobro exitoso! Se han procesado %.2f€ mediante %s", 
                    transaccion.getImporteNeto(), metodoPago));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el cobro: " + e.getMessage());
        }
        
        return "redirect:/proveedor/ventas";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Integer id, Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario proveedor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!juego.getProveedor().getId().equals(proveedor.getId())) {
            return "redirect:/proveedor/mis-juegos";
        }
        
        model.addAttribute("juego", juego);
        return "proveedor/editar-juego";
    }
    
    @PostMapping("/editar/{id}")
    public String editarJuego(@PathVariable Integer id,
                              @RequestParam(value = "descripcion", required = true) String descripcion,
                              @RequestParam(value = "videoUrl", required = false) String videoUrl,
                              @RequestParam(value = "imagen1", required = false) MultipartFile imagen1,
                              @RequestParam(value = "imagen2", required = false) MultipartFile imagen2,
                              @RequestParam(value = "imagen3", required = false) MultipartFile imagen3,
                              @RequestParam(value = "imagen4", required = false) MultipartFile imagen4,
                              @RequestParam(value = "archivoJuego", required = false) MultipartFile archivoJuego,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario proveedor = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!juego.getProveedor().getId().equals(proveedor.getId())) {
            return "redirect:/proveedor/mis-juegos";
        }
        
        try {
            // Actualizar imágenes en Azure Blob Storage
            
            // Actualizar imagen 1 (si se sube nueva)
            if (imagen1 != null && !imagen1.isEmpty()) {
                String validationResult = validateImage(imagen1, "Imagen 1");
                if (validationResult != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult);
                    return "redirect:/proveedor/editar/" + id;
                }
                System.out.println("=== Actualizando imagen 1 ===");
                System.out.println("URL anterior: " + juego.getImagenPortadaUrl());
                String url1 = azureStorageService.updateFile(juego.getImagenPortadaUrl(), imagen1, "juegos");
                System.out.println("Nueva URL: " + url1);
                if (url1 != null) {
                    juego.setImagenPortadaUrl(url1);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Error al subir Imagen 1 a Azure");
                    return "redirect:/proveedor/editar/" + id;
                }
            }
            
            // Actualizar imagen 2 (si se sube nueva)
            if (imagen2 != null && !imagen2.isEmpty()) {
                String validationResult2 = validateImage(imagen2, "Imagen 2");
                if (validationResult2 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult2);
                    return "redirect:/proveedor/editar/" + id;
                }
                System.out.println("=== Actualizando imagen 2 ===");
                System.out.println("URL anterior: " + juego.getImagen2Url());
                String url2 = azureStorageService.updateFile(juego.getImagen2Url(), imagen2, "juegos");
                System.out.println("Nueva URL: " + url2);
                if (url2 != null) {
                    juego.setImagen2Url(url2);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Error al subir Imagen 2 a Azure");
                    return "redirect:/proveedor/editar/" + id;
                }
            }
            
            // Actualizar imagen 3 (si se sube nueva)
            if (imagen3 != null && !imagen3.isEmpty()) {
                String validationResult3 = validateImage(imagen3, "Imagen 3");
                if (validationResult3 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult3);
                    return "redirect:/proveedor/editar/" + id;
                }
                System.out.println("=== Actualizando imagen 3 ===");
                System.out.println("URL anterior: " + juego.getImagen3Url());
                String url3 = azureStorageService.updateFile(juego.getImagen3Url(), imagen3, "juegos");
                System.out.println("Nueva URL: " + url3);
                if (url3 != null) {
                    juego.setImagen3Url(url3);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Error al subir Imagen 3 a Azure");
                    return "redirect:/proveedor/editar/" + id;
                }
            }
            
            // Actualizar imagen 4 (si se sube nueva)
            if (imagen4 != null && !imagen4.isEmpty()) {
                String validationResult4 = validateImage(imagen4, "Imagen 4");
                if (validationResult4 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult4);
                    return "redirect:/proveedor/editar/" + id;
                }
                System.out.println("=== Actualizando imagen 4 ===");
                System.out.println("URL anterior: " + juego.getImagen4Url());
                String url4 = azureStorageService.updateFile(juego.getImagen4Url(), imagen4, "juegos");
                System.out.println("Nueva URL: " + url4);
                if (url4 != null) {
                    juego.setImagen4Url(url4);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Error al subir Imagen 4 a Azure");
                    return "redirect:/proveedor/editar/" + id;
                }
            }
            
            // Actualizar campos permitidos
            juego.setDescripcion(descripcion);
            juego.setVideoPromocionalUrl(videoUrl);
            
            // Actualizar archivo del juego (ZIP) si se proporcionó uno nuevo
            if (archivoJuego != null && !archivoJuego.isEmpty()) {
                // Validar tamaño (500MB máximo)
                long maxSize = 500L * 1024 * 1024; // 500MB
                if (archivoJuego.getSize() > maxSize) {
                    redirectAttributes.addFlashAttribute("error", "El archivo del juego es demasiado grande. Máximo: 500MB");
                    return "redirect:/proveedor/editar/" + id;
                }
                
                // Validar extensión
                String fileName = archivoJuego.getOriginalFilename();
                if (fileName == null || !fileName.matches(".*\\.(zip|rar|7z)$")) {
                    redirectAttributes.addFlashAttribute("error", "El archivo debe ser .zip, .rar o .7z");
                    return "redirect:/proveedor/editar/" + id;
                }
                
                // Si ya existe un archivo, eliminarlo primero (opcional - Azure sobrescribe automáticamente)
                // Subir nuevo archivo a Azure Blob Storage
                String archivoUrl = azureStorageService.uploadFile(archivoJuego, "juegos-archivos");
                String archivoNombre = archivoJuego.getOriginalFilename();
                Integer archivoTamanio = (int) (archivoJuego.getSize() / (1024 * 1024)); // Convertir a MB
                
                juego.setArchivoDescargableUrl(archivoUrl);
                juego.setArchivoNombre(archivoNombre);
                juego.setArchivoTamanioBytes(archivoTamanio);
                
                System.out.println("📦 Archivo actualizado: " + archivoNombre + " (" + archivoTamanio + " MB)");
            }
            
            // NO ACTUALIZAR: título, precio, proveedor, fecha publicación
            juegoRepository.save(juego);
            
            redirectAttributes.addFlashAttribute("success", "Juego actualizado correctamente");
            return "redirect:/proveedor/mis-juegos";
            
        } catch (IOException e) {
            String errorMessage = "Error al actualizar las imágenes en Azure: " + e.getMessage();
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/proveedor/editar/" + id;
        }
    }
    
    /**
     * Valida una imagen y devuelve información sobre sus dimensiones y tamaño
     * @param file Archivo de imagen a validar
     * @param imageName Nombre descriptivo de la imagen
     * @return null si es válida, mensaje de error si no es válida
     */
    private String validateImage(MultipartFile file, String imageName) {
        try {
            // Validar tamaño del archivo (10MB máximo)
            long maxSize = 10 * 1024 * 1024; // 10MB en bytes
            if (file.getSize() > maxSize) {
                return imageName + " es demasiado grande. Máximo: 10MB. Tu archivo: " + 
                       String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0));
            }
            
            // Leer dimensiones de la imagen
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                return imageName + " no es un archivo de imagen válido";
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            // Información para el usuario (no es error, solo informativo)
            // Recomendaciones: ancho mínimo 300px, máximo 2000px
            if (width < 200 || height < 200) {
                return imageName + " es muy pequeña. Mínimo recomendado: 200x200px. " +
                       "Tu imagen: " + width + "x" + height + "px (" + 
                       String.format("%.0f KB", file.getSize() / 1024.0) + ")";
            }
            
            if (width > 3000 || height > 3000) {
                return imageName + " es muy grande. Máximo recomendado: 3000x3000px. " +
                       "Tu imagen: " + width + "x" + height + "px (" + 
                       String.format("%.0f KB", file.getSize() / 1024.0) + ")";
            }
            
            // Todo OK - imagen válida
            return null;
            
        } catch (IOException e) {
            return "Error al procesar " + imageName + ". Verifica que sea un archivo de imagen válido (JPG, PNG, WEBP).";
        }
    }
    
}
