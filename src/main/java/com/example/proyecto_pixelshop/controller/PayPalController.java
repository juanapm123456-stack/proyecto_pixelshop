package com.example.proyecto_pixelshop.controller;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.service.interfaz.IServicioPayPal;
import com.example.proyecto_pixelshop.service.interfaz.IServicioEmail;
import com.example.proyecto_pixelshop.service.interfaz.IServicioJuego;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import com.example.proyecto_pixelshop.service.interfaz.IServicioCompra;
import com.paypal.orders.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/paypal")
public class PayPalController extends BaseController {

    @Autowired private IServicioPayPal payPalService;
    @Autowired private IServicioJuego juegoService;
    @Autowired private IServicioUsuario usuarioService;
    @Autowired private IServicioCompra compraService;
    @Autowired private IServicioEmail emailService;
    
    // Muestra la página de checkout con opciones de pago
    @GetMapping("/checkout/{juegoId}")
    public String checkout(@PathVariable Integer juegoId, 
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Juego juego = juegoService.buscarPorId(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        // Verificar que no lo tenga ya
        if (compraService.usuarioComproJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/juego/" + juegoId;
        }

        model.addAttribute("juego", juego);
        model.addAttribute("usuario", usuario);
        model.addAttribute("paypalClientId", System.getenv("PAYPAL_CLIENT_ID"));

        return "checkout/payment-options";
    }

    // Crea una orden de pago con PayPal
    @PostMapping("/create-order/{juegoId}")
    @ResponseBody
    public String createOrder(@PathVariable Integer juegoId) {
        try {
            System.out.println("=== Creando orden PayPal para juego ID: " + juegoId);
            
            Juego juego = juegoService.buscarPorId(juegoId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

            System.out.println("Juego encontrado: " + juego.getTitulo() + " - Precio: " + juego.getPrecio());
            
            String returnUrl = "http://localhost:8080/paypal/success?juegoId=" + juegoId;
            String cancelUrl = "http://localhost:8080/paypal/cancel?juegoId=" + juegoId;

            String orderId = payPalService.crearOrden(juego, returnUrl, cancelUrl);
            
            System.out.println("Orden PayPal creada exitosamente: " + orderId);
            return orderId;

        } catch (Exception e) {
            System.err.println("ERROR al crear orden de PayPal: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al crear orden de PayPal: " + e.getMessage());
        }
    }

    // Maneja el retorno exitoso desde PayPal
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String token,
                                @RequestParam Integer juegoId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INICIO PayPal Success ===");
            System.out.println("Token PayPal: " + token);
            System.out.println("Juego ID: " + juegoId);
            
            // Capturar el pago
            Order order = payPalService.capturarPago(token);
            System.out.println(" Pago capturado exitosamente");

            if (payPalService.esPaymentCompletado(order)) {
                System.out.println(" Payment status: COMPLETED");
                
                // Obtener datos
                String email = obtenerEmailDelUsuario(authentication);
                System.out.println("Usuario email: " + email);
                
                Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                System.out.println(" Usuario encontrado ID: " + usuario.getId());

                Juego juego = juegoService.buscarPorId(juegoId)
                    .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
                System.out.println(" Juego encontrado: " + juego.getTitulo());

                // Usar el precio del juego como monto pagado (más confiable que parsear respuesta de PayPal)
                Double montoPagado = juego.getPrecio();
                System.out.println(" Monto pagado: " + montoPagado + "€");

                // Crear y completar compra directamente (auto-crea transacciones)
                System.out.println(" Creando compra en BD...");
                Compra compra = compraService.crear(usuario, juego, montoPagado, "Pago Online", order.id());
                System.out.println(" Compra creada ID: " + compra.getId() + " - Estado: " + compra.getEstado());
                
                System.out.println(" Completando compra...");
                compraService.completar(compra.getId());
                System.out.println(" Compra completada exitosamente");
                
                // Enviar email de confirmación
                try {
                    emailService.enviarConfirmacionCompra(compra);
                    System.out.println(" Email de confirmación enviado correctamente");
                } catch (Exception e) {
                    System.err.println(" Error al enviar email de confirmación: " + e.getMessage());
                }
                
                System.out.println("=== FIN PayPal Success (ÉXITO) ===");
                redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
                return "redirect:/mi-biblioteca";
                
            } else {
                System.err.println(" Payment status NO es COMPLETED: " + order.status());
                redirectAttributes.addFlashAttribute("error", "El pago no se completó correctamente");
                return "redirect:/paypal/checkout/" + juegoId;
            }

        } catch (Exception e) {
            System.err.println(" ERROR en PayPal Success: " + e.getClass().getName());
            System.err.println(" Mensaje: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/paypal/checkout/" + juegoId;
        }
    }

    // Maneja la cancelación del pago
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam Integer juegoId,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("warning", "Has cancelado el pago");
        return "redirect:/juego/" + juegoId;
    }

    // Simula pago con tarjeta (solo para desarrollo)
    @PostMapping("/card-payment/{juegoId}")
    public String cardPayment(@PathVariable Long juegoId,
                             @RequestParam String cardNumber,
                             @RequestParam String cardName,
                             @RequestParam String expiryDate,
                             @RequestParam String cvv,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        
        // Obtener email del usuario autenticado (aunque no se usa en esta simulación)
        String email = obtenerEmailDelUsuario(authentication);
        
        // Simulación de diferentes escenarios según el número de tarjeta
        String lastDigits = cardNumber.substring(cardNumber.length() - 4);

        switch (lastDigits) {
            case "1111": // Tarjeta rechazada
                redirectAttributes.addFlashAttribute("error", " Tarjeta rechazada. Por favor, usa otro método de pago.");
                return "redirect:/paypal/checkout/" + juegoId;

            case "2222": // Tarjeta caducada
                redirectAttributes.addFlashAttribute("error", " Tarjeta caducada. Verifica la fecha de expiración.");
                return "redirect:/paypal/checkout/" + juegoId;

            case "3333": // Fondos insuficientes
                redirectAttributes.addFlashAttribute("error", " Fondos insuficientes. No se pudo completar la transacción.");
                return "redirect:/paypal/checkout/" + juegoId;

            default: // Pago exitoso
                redirectAttributes.addFlashAttribute("success", " Pago procesado exitosamente con tarjeta");
                return "redirect:/compra/confirmar/" + juegoId;
        }
    }
}
