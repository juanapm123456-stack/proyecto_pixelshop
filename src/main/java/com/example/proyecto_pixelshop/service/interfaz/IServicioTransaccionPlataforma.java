package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.TransaccionPlataforma;
import com.example.proyecto_pixelshop.model.enums.TipoTransaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

// Interfaz de servicio para gestionar transacciones de la plataforma
public interface IServicioTransaccionPlataforma {
    
    // Busca una transacción por su ID
    Optional<TransaccionPlataforma> buscarPorId(Integer id);
    
    // Lista todas las transacciones ordenadas por fecha (sin paginación)
    List<TransaccionPlataforma> listarTodas();
    
    // Lista todas las transacciones con paginación
    Page<TransaccionPlataforma> listarTodasPaginadas(Pageable pageable);
    
    // Lista transacciones por tipo
    List<TransaccionPlataforma> listarPorTipo(TipoTransaccion tipoTransaccion);
    
    // Busca transacciones de comisión por ID de compra
    List<TransaccionPlataforma> buscarComisionPorCompra(Integer compraId);
    
    // Busca transacciones de publicación por ID de juego
    List<TransaccionPlataforma> buscarPagoPublicacionPorJuego(Integer juegoId);
    
    // Registra una comisión de venta (15% de la compra)
    TransaccionPlataforma registrarComisionVenta(Integer compraId, Double monto);
    
    // Registra un pago de publicación (25€ por publicar juego)
    TransaccionPlataforma registrarPagoPublicacion(Integer juegoId, Double monto);
    
    // Guarda o actualiza una transacción
    TransaccionPlataforma guardar(TransaccionPlataforma transaccion);
    
    // Calcula los ingresos totales de la plataforma
    Double calcularIngresosTotales();
    
    // Calcula el total de comisiones de ventas
    Double calcularComisionesVentas();
    
    // Calcula el total de pagos de publicación
    Double calcularPagosPublicacion();
    
    // Cuenta transacciones por tipo
    long contarPorTipo(TipoTransaccion tipoTransaccion);
}
