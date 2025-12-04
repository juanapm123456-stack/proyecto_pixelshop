package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Usuario;

// Interfaz para operaciones de Email
public interface IServicioEmail {
    
    // Envía email de bienvenida al registrarse
    void enviarEmailBienvenida(Usuario usuario);
    
    // Envía email de confirmación de compra
    void enviarConfirmacionCompra(Compra compra);
    
    // Envía email de notificación de venta al proveedor
    void enviarNotificacionVenta(Compra compra);
}
