package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.EstadoCompra;

import java.util.List;
import java.util.Optional;

// Interfaz de servicio para gestionar compras
public interface IServicioCompra {
    
    // Busca una compra por su ID
    Optional<Compra> buscarPorId(Integer id);
    
    // Busca una compra específica entre usuario y juego
    Optional<Compra> buscarPorUsuarioYJuego(Usuario usuario, Juego juego);
    
    // Lista todas las compras
    List<Compra> listarTodas();
    
    // Lista todas las compras de un usuario
    List<Compra> listarPorUsuario(Usuario usuario);
    
    // Lista todas las compras de un juego
    List<Compra> listarPorJuego(Juego juego);
    
    // Lista la biblioteca de un usuario (compras completadas)
    List<Compra> obtenerBiblioteca(Usuario usuario);
    
    // Lista compras por estado
    List<Compra> listarPorEstado(EstadoCompra estado);
    
    // Lista todas las compras de los juegos de un proveedor
    List<Compra> listarPorProveedor(Usuario proveedor);
    
    // Verifica si un usuario ya compró un juego
    boolean usuarioComproJuego(Usuario usuario, Juego juego);
    
    // Crea una nueva compra (estado PENDIENTE)
    Compra crear(Usuario usuario, Juego juego, Double precioPagado, String metodoPago, String orderIdPaypal);
    
    // Completa una compra (cambia estado a COMPLETADA y crea transacciones)
    Compra completar(Integer compraId);
    
    // Reembolsa una compra (cambia estado a REEMBOLSADA)
    Compra reembolsar(Integer compraId);
    
    // Guarda o actualiza una compra
    Compra guardar(Compra compra);
    
    // Elimina una compra
    void eliminar(Integer id);
    
    // Calcula el total de ventas (compras completadas)
    Double calcularTotalVentas();
    
    // Calcula las ventas de un proveedor específico
    Double calcularVentasProveedor(Usuario proveedor);
    
    // Cuenta compras por estado
    long contarPorEstado(EstadoCompra estado);
}
