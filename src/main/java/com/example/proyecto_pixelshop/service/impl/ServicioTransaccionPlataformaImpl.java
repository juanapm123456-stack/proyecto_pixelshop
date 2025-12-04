package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.TransaccionPlataforma;
import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.enums.TipoTransaccion;
import com.example.proyecto_pixelshop.repository.TransaccionPlataformaRepository;
import com.example.proyecto_pixelshop.repository.CompraRepository;
import com.example.proyecto_pixelshop.repository.JuegoRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioTransaccionPlataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioTransaccionPlataformaImpl implements IServicioTransaccionPlataforma {
    
    @Autowired private TransaccionPlataformaRepository transaccionRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private JuegoRepository juegoRepository;
    
    // Busca una transacción de plataforma por su ID
    @Override
    @Transactional(readOnly = true)
    public Optional<TransaccionPlataforma> buscarPorId(Integer id) {
        return transaccionRepository.findById(id);
    }
    
    // Lista todas las transacciones de la plataforma ordenadas por fecha (más recientes primero)
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPlataforma> listarTodas() {
        return transaccionRepository.findAllOrderByFechaDescList();
    }
    
    // Lista todas las transacciones con paginación
    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionPlataforma> listarTodasPaginadas(Pageable pageable) {
        return transaccionRepository.findAllOrderByFechaDesc(pageable);
    }
    
    // Lista las transacciones de un tipo específico (COMISION_VENTA o PAGO_PUBLICACION)
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPlataforma> listarPorTipo(TipoTransaccion tipoTransaccion) {
        return transaccionRepository.findByTipoTransaccion(tipoTransaccion);
    }
    
    // Busca la transacción de comisión asociada a una compra específica
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPlataforma> buscarComisionPorCompra(Integer compraId) {
        return transaccionRepository.findComisionByCompraId(compraId);
    }
    
    // Busca las transacciones de pago de publicación de un usuario (proveedor)
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPlataforma> buscarPagoPublicacionPorJuego(Integer juegoId) {
        // ⭐ Ya no se puede buscar por juego_id directamente
        // Buscar por usuario (proveedor del juego)
        Juego juego = juegoRepository.findById(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        return transaccionRepository.findPagoPublicacionByUsuarioId(juego.getProveedor().getId());
    }
    
    // Registra una comisión de venta (15% de cada compra) en los ingresos de la plataforma
    @Override
    public TransaccionPlataforma registrarComisionVenta(Integer compraId, Double importe) {
        Compra compra = compraRepository.findById(compraId)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        
        Double porcentajeComision = 15.0;
        
        // ⭐ Usar constructor con Compra (relación 1:1)
        TransaccionPlataforma transaccion = new TransaccionPlataforma(
            TipoTransaccion.COMISION_VENTA,
            importe,
            compra.getJuego().getProveedor(),  // Usuario es el proveedor
            compra,  // ⭐ Relación 1:1 con Compra
            "Comisión 15% por venta de " + compra.getJuego().getTitulo(),
            porcentajeComision
        );
        
        return transaccionRepository.save(transaccion);
    }
    
    // Registra el pago de 25€ por publicar un juego en la plataforma
    @Override
    public TransaccionPlataforma registrarPagoPublicacion(Integer juegoId, Double importe) {
        Juego juego = juegoRepository.findById(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // ⭐ Constructor simplificado (sin Compra, sin Juego directo)
        TransaccionPlataforma transaccion = new TransaccionPlataforma(
            TipoTransaccion.PAGO_PUBLICACION,
            importe,
            juego.getProveedor(),
            "Pago por publicación de " + juego.getTitulo()
        );
        // Nota: No tiene compra asociada para PAGO_PUBLICACION
        
        return transaccionRepository.save(transaccion);
    }
    
    // Guarda o actualiza una transacción de plataforma
    @Override
    public TransaccionPlataforma guardar(TransaccionPlataforma transaccion) {
        return transaccionRepository.save(transaccion);
    }
    
    // Calcula el total de ingresos de la plataforma (comisiones + pagos de publicación)
    @Override
    @Transactional(readOnly = true)
    public Double calcularIngresosTotales() {
        Double total = transaccionRepository.calcularIngresosTotales();
        return total != null ? total : 0.0;
    }
    
    // Calcula el total de comisiones generadas por ventas (15% de cada compra)
    @Override
    @Transactional(readOnly = true)
    public Double calcularComisionesVentas() {
        Double total = transaccionRepository.calcularComisionesVentas();
        return total != null ? total : 0.0;
    }
    
    // Calcula el total de ingresos por pagos de publicación (25â‚¬ por juego)
    @Override
    @Transactional(readOnly = true)
    public Double calcularPagosPublicacion() {
        Double total = transaccionRepository.calcularPagosPublicacion();
        return total != null ? total : 0.0;
    }
    
    // Cuenta cuántas transacciones hay de un tipo específico
    @Override
    @Transactional(readOnly = true)
    public long contarPorTipo(TipoTransaccion tipoTransaccion) {
        return transaccionRepository.countByTipoTransaccion(tipoTransaccion);
    }
}
