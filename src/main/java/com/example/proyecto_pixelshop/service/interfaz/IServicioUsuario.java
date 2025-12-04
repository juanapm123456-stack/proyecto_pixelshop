package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;

import java.util.List;
import java.util.Optional;

// Interfaz de servicio para gestionar usuarios
public interface IServicioUsuario {
    
    // Busca un usuario por su ID
    Optional<Usuario> buscarPorId(Integer id);
    
    // Busca un usuario por su email
    Optional<Usuario> buscarPorEmail(String email);
    
    // Verifica si existe un email registrado
    boolean existeEmail(String email);
    
    // Guarda o actualiza un usuario
    Usuario guardar(Usuario usuario);
    
    // Registra un nuevo usuario (encripta contraseña)
    Usuario registrar(Usuario usuario);
    
    // Actualiza los datos de un usuario existente
    Usuario actualizar(Usuario usuario);
    
    // Elimina un usuario por su ID
    void eliminar(Integer id);
    
    // Lista todos los usuarios
    List<Usuario> listarTodos();
    
    // Lista usuarios por rol
    List<Usuario> listarPorRol(Rol rol);
    
    // Lista todos los proveedores
    List<Usuario> listarProveedores();
    
    // Lista todos los clientes
    List<Usuario> listarClientes();
    
    // Cuenta el total de usuarios
    long contarTodos();
    
    // Cuenta usuarios por rol
    long contarPorRol(Rol rol);
    
    // Activa o desactiva un usuario
    void cambiarEstado(Integer id, Boolean activo);
}
