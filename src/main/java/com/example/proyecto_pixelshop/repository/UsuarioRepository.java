package com.example.proyecto_pixelshop.repository;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Busca un usuario ACTIVO por su email
    Optional<Usuario> findByEmailAndActivo(String email, Boolean activo);
    
    // Busca un usuario por su email (sin filtrar por activo)
    Optional<Usuario> findByEmail(String email);

    // Verifica si existe un email en la base de datos (activo o inactivo)
    boolean existsByEmail(String email);
    
    // Verifica si existe un email ACTIVO en la base de datos
    boolean existsByEmailAndActivo(String email, Boolean activo);

    // Busca todos los usuarios ACTIVOS por rol
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    List<Usuario> findByRol(Rol rol);
    
    // Busca usuarios por estado activo/inactivo
    List<Usuario> findByActivo(Boolean activo);
    
    // Busca usuarios activos por rol
    List<Usuario> findByActivoAndRol(Boolean activo, Rol rol);

    // Busca todos los proveedores ACTIVOS (usuarios con rol PROVEEDOR)
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'PROVEEDOR' AND u.activo = true")
    List<Usuario> findAllProveedores();

    // Busca todos los clientes ACTIVOS (usuarios con rol CLIENTE)
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'CLIENTE' AND u.activo = true")
    List<Usuario> findAllClientes();
    
    // Busca todos los usuarios ACTIVOS
    @Query("SELECT u FROM Usuario u WHERE u.activo = true")
    List<Usuario> findAllActive();

    // Cuenta el total de usuarios ACTIVOS
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    long countActive();

    // Cuenta los usuarios ACTIVOS por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    long countByRolActive(Rol rol);
}
