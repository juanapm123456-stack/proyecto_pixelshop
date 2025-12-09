package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.TransaccionProveedor;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.enums.EstadoPago;
import com.example.proyecto_pixelshop.repository.TransaccionProveedorRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioTransaccionProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioTransaccionProveedorImpl implements IServicioTransaccionProveedor {
    
    @Autowired private TransaccionProveedorRepository transaccionRepository;
    
    // ========================================
    // MÉTODOS DE BÚSQUEDA Y LISTADO
    // ========================================
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TransaccionProveedor> buscarPorId(Integer id) {
        return transaccionRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionProveedor> listarPorProveedor(Usuario proveedor) {
        return transaccionRepository.findByUsuarioOrderByFechaVentaDesc(proveedor);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionProveedor> listarPendientesPorProveedor(Usuario proveedor) {
        return transaccionRepository.findByUsuarioAndEstadoPago(proveedor, EstadoPago.PENDIENTE);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionProveedor> listarPagadasPorProveedor(Usuario proveedor) {
        return transaccionRepository.findByUsuarioAndEstadoPagoOrderByFechaPagoDesc(proveedor, EstadoPago.PAGADO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionProveedor> listarPorEstado(EstadoPago estadoPago) {
        return transaccionRepository.findByEstadoPago(estadoPago);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionProveedor> listarTodasPendientes() {
        return transaccionRepository.findByEstadoPagoOrderByFechaVentaAsc(EstadoPago.PENDIENTE);
    }
    
    // ========================================
    // MÉTODOS DE CREACIÓN Y ACTUALIZACIÓN
    // ========================================
    
    @Override
    public TransaccionProveedor crear(Compra compra) {
        Double importeBruto = compra.getPrecioPagado();
        Double porcentajeComision = 15.0;
        
        // CÁLCULOS EN EL SERVICIO (no en el modelo)
        Double importeComision = redondear(importeBruto * (porcentajeComision / 100.0));
        Double importeNeto = redondear(importeBruto - importeComision);
        
        //  Ahora solo necesita Compra y Usuario (el juego se obtiene desde compra)
        TransaccionProveedor transaccion = new TransaccionProveedor(
            compra,
            compra.getJuego().getProveedor(),  // Usuario (proveedor)
            redondear(importeBruto),
            porcentajeComision
        );
        
        transaccion.setImporteComision(importeComision);
        transaccion.setImporteNeto(importeNeto);
        transaccion.setEstadoPago(EstadoPago.PENDIENTE);
        transaccion.setFechaVenta(LocalDateTime.now());
        
        return transaccionRepository.save(transaccion);
    }
    
    // ========================================
    // MÉTODOS DE GESTIÓN DE PAGOS
    // ========================================
    
    @Override
    public TransaccionProveedor marcarComoPagada(Integer transaccionId) {
        TransaccionProveedor transaccion = transaccionRepository.findById(transaccionId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + transaccionId));
        
        if (transaccion.getEstadoPago() == EstadoPago.PAGADO) {
            throw new RuntimeException("La transacción ya está marcada como pagada");
        }
        
        transaccion.setEstadoPago(EstadoPago.PAGADO);
        transaccion.setFechaPago(LocalDateTime.now());
        
        return transaccionRepository.save(transaccion);
    }
    
    // Marca múltiples transacciones como pagadas (procesa en lote)
    @Override
    public List<TransaccionProveedor> marcarComoaPagadas(List<Integer> transaccionIds) {
        List<TransaccionProveedor> transaccionesPagadas = new ArrayList<>();
        
        for (Integer id : transaccionIds) {
            try {
                TransaccionProveedor transaccion = marcarComoPagada(id);
                transaccionesPagadas.add(transaccion);
            } catch (RuntimeException e) {
                // Log error pero continuar con las demás transacciones
                System.err.println("Error al marcar transacción " + id + " como pagada: " + e.getMessage());
            }
        }
        
        return transaccionesPagadas;
    }
    
    @Override
    public TransaccionProveedor guardar(TransaccionProveedor transaccion) {
        return transaccionRepository.save(transaccion);
    }
    
    // ========================================
    // MÉTODOS DE CÁLCULO FINANCIERO
    // ========================================
    
    @Override
    @Transactional(readOnly = true)
    public Double calcularMontoPendiente(Usuario proveedor) {
        Double total = transaccionRepository.sumarImporteNetoPorUsuarioYEstado(proveedor, EstadoPago.PENDIENTE);
        return total != null ? total : 0.0;
    }
    
    // Calcula el total de dinero ya pagado a un proveedor
    @Override
    @Transactional(readOnly = true)
    public Double calcularMontoPagado(Usuario proveedor) {
        Double total = transaccionRepository.sumarImporteNetoPorUsuarioYEstado(proveedor, EstadoPago.PAGADO);
        return total != null ? total : 0.0;
    }
    
    // Calcula el monto bruto total de ventas de un proveedor (antes de comisiones)
    @Override
    @Transactional(readOnly = true)
    public Double calcularVentasTotales(Usuario proveedor) {
        Double total = transaccionRepository.sumarImporteBrutoPorUsuario(proveedor);
        return total != null ? total : 0.0;
    }
    
    // Calcula el total de comisiones (15%) generadas por las ventas de un proveedor
    @Override
    @Transactional(readOnly = true)
    public Double calcularComisionesTotales(Usuario proveedor) {
        Double total = transaccionRepository.sumarImporteComisionPorUsuario(proveedor);
        return total != null ? total : 0.0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoPago estadoPago) {
        return transaccionRepository.countByEstadoPago(estadoPago);
    }
    
    // ========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================
    
    private Double redondear(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
