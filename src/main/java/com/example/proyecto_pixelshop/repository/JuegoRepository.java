package com.example.proyecto_pixelshop.repository;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Integer> {

    // Busca todos los juegos activos
    List<Juego> findByActivoTrue();

    // Busca todos los juegos de un proveedor específico
    List<Juego> findByProveedor(Usuario proveedor);

    // Busca juegos activos por título (búsqueda parcial, case-insensitive)
    List<Juego> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo);

    // Busca juegos activos por género
    List<Juego> findByGeneroAndActivoTrue(String genero);

    // Busca juegos activos de un proveedor
    List<Juego> findByProveedorAndActivoTrue(Usuario proveedor);

    // Calcula el total de pagos de publicación pendientes de un proveedor
    @Query("SELECT SUM(j.montoPublicacion) FROM Juego j WHERE j.proveedor = :proveedor AND j.activo = true")
    Double calcularPagosPublicacionProveedor(Usuario proveedor);

    // Busca todos los juegos publicados (sin importar si están activos)
    @Query("SELECT j FROM Juego j WHERE j.fechaPublicacion IS NOT NULL ORDER BY j.fechaPublicacion DESC")
    List<Juego> findAllPublicados();

    // Cuenta el total de juegos activos
    long countByActivoTrue();

    // Elimina todos los juegos de un proveedor
    @Transactional
    void deleteByProveedor(Usuario proveedor);
}
