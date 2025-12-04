package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.TransaccionPlataforma;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.service.interfaz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioTransaccionPlataforma transaccionPlataformaService;
    @Autowired private IServicioJuego juegoService;
    
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
    
    @PostMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        usuarioService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        return "redirect:/admin/usuarios";
    }
    
    @GetMapping("/ganancias")
    public String ganancias(Model model) {
        // Calcular ganancias totales desde TransaccionPlataforma
        Double gananciaComisiones = transaccionPlataformaService.calcularComisionesVentas();
        Double gananciaPublicaciones = transaccionPlataformaService.calcularPagosPublicacion();
        Double gananciaTotal = transaccionPlataformaService.calcularIngresosTotales();
        Double totalVentas = compraService.calcularTotalVentas();
        
        // Contar total de juegos publicados (todos los juegos en la base de datos)
        Long totalPublicaciones = (long) juegoService.listarTodos().size();
        
        model.addAttribute("gananciaVentas", gananciaComisiones);
        model.addAttribute("gananciaPublicaciones", gananciaPublicaciones);
        model.addAttribute("gananciaTotal", gananciaTotal);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalPublicaciones", totalPublicaciones);
        
        return "admin/ganancias";
    }
    
    @GetMapping("/movimientos")
    public String movimientos(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        // Configurar paginación: 10 registros por página
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);
        
        // Obtener transacciones paginadas
        Page<TransaccionPlataforma> transaccionesPage = transaccionPlataformaService.listarTodasPaginadas(pageable);
        
        model.addAttribute("transaccionesPage", transaccionesPage);
        model.addAttribute("transacciones", transaccionesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transaccionesPage.getTotalPages());
        model.addAttribute("totalItems", transaccionesPage.getTotalElements());
        model.addAttribute("compraService", compraService);
        model.addAttribute("juegoService", juegoService);
        
        return "admin/movimientos";
    }
}
