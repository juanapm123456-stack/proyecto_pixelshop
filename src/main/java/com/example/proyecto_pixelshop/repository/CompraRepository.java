package com.example.proyecto_pixelshop.repository;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.enums.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    
    
    // Buscar por usuario ordenado por fecha
    List<Compra> findByUsuarioOrderByFechaCompraDesc(Usuario usuario);

    // Buscar por juego
    List<Compra> findByJuego(Juego juego);

    // Verificar existencia
    boolean existsByUsuarioAndJuego(Usuario usuario, Juego juego);

    // Buscar compra específica
    Optional<Compra> findByUsuarioAndJuego(Usuario usuario, Juego juego);

    // Buscar por usuario y estado (biblioteca = COMPLETADA)
    List<Compra> findByUsuarioAndEstado(Usuario usuario, EstadoCompra estado);

    // Buscar por estado
    List<Compra> findByEstado(EstadoCompra estado);

    // Contar por estado
    long countByEstado(EstadoCompra estado);
    
    // Contar por usuario
    long countByUsuario(Usuario usuario);
    
    // Eliminar por usuario
    @Transactional
    void deleteByUsuario(Usuario usuario);

   
    
    // Sumar ventas totales
    @Query("SELECT SUM(c.precioPagado) FROM Compra c WHERE c.estado = 'COMPLETADA'")
    Double sumarVentasTotales();

    // Sumar ventas de un proveedor
    @Query("SELECT SUM(c.precioPagado) FROM Compra c WHERE c.juego.proveedor = :proveedor AND c.estado = 'COMPLETADA'")
    Double sumarVentasPorProveedor(@Param("proveedor") Usuario proveedor);

    // Buscar compras de un proveedor (a través de sus juegos)
    @Query("SELECT c FROM Compra c WHERE c.juego.proveedor = :proveedor ORDER BY c.fechaCompra DESC")
    List<Compra> findComprasPorProveedor(@Param("proveedor") Usuario proveedor);
}
