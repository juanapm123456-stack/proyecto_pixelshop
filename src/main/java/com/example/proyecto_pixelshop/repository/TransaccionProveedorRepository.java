package com.example.proyecto_pixelshop.repository;

import com.example.proyecto_pixelshop.model.TransaccionProveedor;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionProveedorRepository extends JpaRepository<TransaccionProveedor, Integer> {

    // Buscar por usuario ordenado por fecha
    List<TransaccionProveedor> findByUsuarioOrderByFechaVentaDesc(Usuario usuario);
    
    // Buscar por usuario y estado
    List<TransaccionProveedor> findByUsuarioAndEstadoPago(Usuario usuario, EstadoPago estado);
    
    // Buscar por usuario y estado ordenado por fecha de pago
    List<TransaccionProveedor> findByUsuarioAndEstadoPagoOrderByFechaPagoDesc(Usuario usuario, EstadoPago estado);
    
    // Buscar por estado
    List<TransaccionProveedor> findByEstadoPago(EstadoPago estado);
    
    // Buscar pendientes ordenadas por fecha ascendente
    List<TransaccionProveedor> findByEstadoPagoOrderByFechaVentaAsc(EstadoPago estado);
    
    // Contar por estado
    long countByEstadoPago(EstadoPago estado);
    
    // Solo queries complejas con agregaciones necesitan @Query
    @Query("SELECT SUM(t.importeNeto) FROM TransaccionProveedor t WHERE t.usuario = :usuario AND t.estadoPago = :estado")
    Double sumarImporteNetoPorUsuarioYEstado(@Param("usuario") Usuario usuario, @Param("estado") EstadoPago estado);
    
    @Query("SELECT SUM(t.importeBruto) FROM TransaccionProveedor t WHERE t.usuario = :usuario")
    Double sumarImporteBrutoPorUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT SUM(t.importeComision) FROM TransaccionProveedor t WHERE t.usuario = :usuario")
    Double sumarImporteComisionPorUsuario(@Param("usuario") Usuario usuario);
}
