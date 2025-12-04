package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.TransaccionProveedor;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.enums.EstadoPago;

import java.util.List;
import java.util.Optional;

// Interfaz de servicio para gestionar transacciones de proveedores
public interface IServicioTransaccionProveedor {
    
    // Busca una transacción por su ID
    Optional<TransaccionProveedor> buscarPorId(Integer id);
    
    // Lista todas las transacciones de un proveedor
    List<TransaccionProveedor> listarPorProveedor(Usuario proveedor);
    
    // Lista transacciones pendientes de un proveedor
    List<TransaccionProveedor> listarPendientesPorProveedor(Usuario proveedor);
    
    // Lista transacciones pagadas de un proveedor
    List<TransaccionProveedor> listarPagadasPorProveedor(Usuario proveedor);
    
    // Lista todas las transacciones por estado
    List<TransaccionProveedor> listarPorEstado(EstadoPago estadoPago);
    
    // Lista todas las transacciones pendientes globalmente
    List<TransaccionProveedor> listarTodasPendientes();
    
    // Crea una transacción a partir de una compra (auto-calcula comisión 15% y monto neto 85%)
    TransaccionProveedor crear(Compra compra);
    
    // Marca una transacción como pagada
    TransaccionProveedor marcarComoPagada(Integer transaccionId);
    
    // Marca múltiples transacciones como pagadas
    List<TransaccionProveedor> marcarComoaPagadas(List<Integer> transaccionIds);
    
    // Guarda o actualiza una transacción
    TransaccionProveedor guardar(TransaccionProveedor transaccion);
    
    // Calcula el monto total pendiente de un proveedor
    Double calcularMontoPendiente(Usuario proveedor);
    
    // Calcula el monto total pagado a un proveedor
    Double calcularMontoPagado(Usuario proveedor);
    
    // Calcula las ventas brutas totales de un proveedor
    Double calcularVentasTotales(Usuario proveedor);
    
    // Calcula las comisiones totales generadas por un proveedor
    Double calcularComisionesTotales(Usuario proveedor);
    
    // Cuenta transacciones por estado
    long contarPorEstado(EstadoPago estadoPago);
}
