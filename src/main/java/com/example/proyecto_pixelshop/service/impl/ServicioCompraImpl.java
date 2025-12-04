package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.EstadoCompra;
import com.example.proyecto_pixelshop.repository.CompraRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioCompra;
import com.example.proyecto_pixelshop.service.interfaz.IServicioTransaccionProveedor;
import com.example.proyecto_pixelshop.service.interfaz.IServicioTransaccionPlataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioCompraImpl implements IServicioCompra {
    
    @Autowired private CompraRepository compraRepository;
    @Autowired private IServicioTransaccionProveedor transaccionProveedorService;
    @Autowired private IServicioTransaccionPlataforma transaccionPlataformaService;
    
    // Busca una compra por su ID en la base de datos
    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorId(Integer id) {
        return compraRepository.findById(id);
    }
    
    // Busca si existe una compra específica de un usuario para un juego determinado
    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorUsuarioYJuego(Usuario usuario, Juego juego) {
        return compraRepository.findByUsuarioAndJuego(usuario, juego);
    }
    
    // Obtiene la lista completa de todas las compras del sistema
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }
    
    // Lista todas las compras de un usuario ordenadas por fecha (más recientes primero)
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorUsuario(Usuario usuario) {
        return compraRepository.findByUsuarioOrderByFechaCompraDesc(usuario);
    }
    
    // Lista todas las compras realizadas de un juego específico
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorJuego(Juego juego) {
        return compraRepository.findByJuego(juego);
    }
    
    // Obtiene la biblioteca de un usuario (compras completadas que puede descargar)
    @Override
    @Transactional(readOnly = true)
    public List<Compra> obtenerBiblioteca(Usuario usuario) {
        return compraRepository.findByUsuarioAndEstado(usuario, EstadoCompra.COMPLETADA);
    }
    
    // Lista todas las compras con un estado específico (PENDIENTE, COMPLETADA, REEMBOLSADA)
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorEstado(EstadoCompra estado) {
        return compraRepository.findByEstado(estado);
    }
    
    // Lista todas las compras de juegos que pertenecen a un proveedor específico
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorProveedor(Usuario proveedor) {
        return compraRepository.findComprasPorProveedor(proveedor);
    }
    
    // Verifica si un usuario ya compró un juego específico
    @Override
    @Transactional(readOnly = true)
    public boolean usuarioComproJuego(Usuario usuario, Juego juego) {
        return compraRepository.existsByUsuarioAndJuego(usuario, juego);
    }
    
    // Crea una nueva compra con estado PENDIENTE (todavía no confirmada)
    @Override
    public Compra crear(Usuario usuario, Juego juego, Double precioPagado, String metodoPago, String orderIdPaypal) {
        // Buscar compra existente para este usuario y juego
        Optional<Compra> compraExistente = compraRepository.findByUsuarioAndJuego(usuario, juego);
        
        // Si existe una compra COMPLETADA, no permitir duplicados
        if (compraExistente.isPresent() && compraExistente.get().getEstado() == EstadoCompra.COMPLETADA) {
            throw new RuntimeException("El usuario ya compró este juego");
        }
        
        // Si existe una compra PENDIENTE anterior, eliminarla (PayPal duplicado/error anterior)
        if (compraExistente.isPresent() && compraExistente.get().getEstado() == EstadoCompra.PENDIENTE) {
            System.out.println(" Eliminando compra PENDIENTE duplicada ID: " + compraExistente.get().getId());
            compraRepository.delete(compraExistente.get());
        }
        
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecioPagado(precioPagado);
        compra.setMetodoPago(metodoPago);
        compra.setIdOrdenPaypal(orderIdPaypal);
        compra.setEstado(EstadoCompra.PENDIENTE);
        compra.setFechaCompra(LocalDateTime.now());
        
        return compraRepository.save(compra);
    }
    
    // Completa una compra: cambia estado a COMPLETADA y crea transacción para el proveedor (85% de comisión)
    @Override
    public Compra completar(Integer compraId) {
        Compra compra = compraRepository.findById(compraId)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + compraId));
        
        if (compra.getEstado() == EstadoCompra.COMPLETADA) {
            throw new RuntimeException("La compra ya está completada");
        }
        
        // Cambiar estado a COMPLETADA
        compra.setEstado(EstadoCompra.COMPLETADA);
        Compra compraActualizada = compraRepository.save(compra);
        
        // Calcular comisión (15% plataforma, 85% proveedor)
        Double precioPagado = compraActualizada.getPrecioPagado();
        Double comisionPlataforma = Math.round(precioPagado * 0.15 * 100.0) / 100.0;
        
        System.out.println("ðŸ’° Registrando transacciones para compra ID: " + compraActualizada.getId());
        System.out.println("   - Precio pagado: " + precioPagado + "â‚¬");
        System.out.println("   - Comisión plataforma (15%): " + comisionPlataforma + "â‚¬");
        System.out.println("   - Monto proveedor (85%): " + (precioPagado * 0.85) + "â‚¬");
        
        // Crear transacción de proveedor (85% para proveedor)
        transaccionProveedorService.crear(compraActualizada);
        System.out.println("âœ… TransaccionProveedor creada");
        
        // Crear transacción de plataforma (15% comisión)
        transaccionPlataformaService.registrarComisionVenta(compraActualizada.getId(), comisionPlataforma);
        System.out.println("âœ… TransaccionPlataforma (comisión) creada");
        
        return compraActualizada;
    }
    
    // Reembolsa una compra: cambia el estado a PENDIENTE (REEMBOLSADA eliminado)
    @Override
    public Compra reembolsar(Integer compraId) {
        Compra compra = compraRepository.findById(compraId)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + compraId));
        
        // Funcionalidad de reembolso deshabilitada
        throw new RuntimeException("Funcionalidad de reembolso no disponible");
        
        // compra.setEstado(EstadoCompra.PENDIENTE);
        // return compraRepository.save(compra);
    }
    
    // Guarda o actualiza una compra en la base de datos
    @Override
    public Compra guardar(Compra compra) {
        return compraRepository.save(compra);
    }
    
    // Elimina una compra de la base de datos por su ID
    @Override
    public void eliminar(Integer id) {
        compraRepository.deleteById(id);
    }
    
    // Calcula el total de ventas en euros de todas las compras completadas del sistema
    @Override
    @Transactional(readOnly = true)
    public Double calcularTotalVentas() {
        Double total = compraRepository.sumarVentasTotales();
        return total != null ? total : 0.0;
    }
    
    // Calcula el total de ventas en euros de los juegos de un proveedor específico
    @Override
    @Transactional(readOnly = true)
    public Double calcularVentasProveedor(Usuario proveedor) {
        Double total = compraRepository.sumarVentasPorProveedor(proveedor);
        return total != null ? total : 0.0;
    }
    
    // Cuenta cuántas compras tienen un estado específico
    @Override
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoCompra estado) {
        return compraRepository.countByEstado(estado);
    }
}
