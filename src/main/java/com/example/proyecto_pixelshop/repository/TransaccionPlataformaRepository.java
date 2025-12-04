package com.example.proyecto_pixelshop.repository;

import com.example.proyecto_pixelshop.model.TransaccionPlataforma;
import com.example.proyecto_pixelshop.model.enums.TipoTransaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionPlataformaRepository extends JpaRepository<TransaccionPlataforma, Integer> {

    // Busca transacciones por tipo
    List<TransaccionPlataforma> findByTipoTransaccion(TipoTransaccion tipo);
    
    // Busca todas las transacciones con paginación (ordenadas por fecha DESC)
    @Query("SELECT t FROM TransaccionPlataforma t ORDER BY t.fechaTransaccion DESC")
    Page<TransaccionPlataforma> findAllOrderByFechaDesc(Pageable pageable);

    // Busca todas las transacciones ordenadas por fecha (sin paginación)
    @Query("SELECT t FROM TransaccionPlataforma t ORDER BY t.fechaTransaccion DESC")
    List<TransaccionPlataforma> findAllOrderByFechaDescList();

    // Calcula el total de ingresos de la plataforma
    @Query("SELECT SUM(t.importe) FROM TransaccionPlataforma t")
    Double calcularIngresosTotales();

    // Calcula el total de comisiones de ventas
    @Query("SELECT SUM(t.importe) FROM TransaccionPlataforma t WHERE t.tipoTransaccion = 'COMISION_VENTA'")
    Double calcularComisionesVentas();

    // Calcula el total de pagos de publicación
    @Query("SELECT SUM(t.importe) FROM TransaccionPlataforma t WHERE t.tipoTransaccion = 'PAGO_PUBLICACION'")
    Double calcularPagosPublicacion();

    // Cuenta transacciones por tipo
    long countByTipoTransaccion(TipoTransaccion tipo);

    // Busca transacciones de comisión por compra
    @Query("SELECT t FROM TransaccionPlataforma t WHERE t.tipoTransaccion = 'COMISION_VENTA' AND t.compra.id = :compraId")
    List<TransaccionPlataforma> findComisionByCompraId(Integer compraId);

    // ⭐ NOTA: Ya no se puede buscar por juego_id directamente
    // Para buscar pagos de publicación, usar descripción o usuario
    @Query("SELECT t FROM TransaccionPlataforma t WHERE t.tipoTransaccion = 'PAGO_PUBLICACION' AND t.usuario.id = :usuarioId")
    List<TransaccionPlataforma> findPagoPublicacionByUsuarioId(Integer usuarioId);
}
