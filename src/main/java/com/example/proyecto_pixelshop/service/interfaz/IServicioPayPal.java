package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.Juego;
import com.paypal.orders.Order;
import java.io.IOException;

// Interfaz para operaciones de PayPal
public interface IServicioPayPal {
    
    // Crea una orden de pago en PayPal
    String crearOrden(Juego juego, String returnUrl, String cancelUrl) throws IOException;
    
    // Captura el pago de una orden aprobada
    Order capturarPago(String orderId) throws IOException;
    
    // Obtiene los detalles de una orden
    Order obtenerDetallesOrden(String orderId) throws IOException;
    
    // Valida si una orden fue completada exitosamente
    boolean esPaymentCompletado(Order order);
    
    // Extrae el monto pagado de una orden
    Double extraerMontoPagado(Order order);
    
    // Extrae el ID del juego de una orden
    Integer extraerJuegoId(Order order);
}
