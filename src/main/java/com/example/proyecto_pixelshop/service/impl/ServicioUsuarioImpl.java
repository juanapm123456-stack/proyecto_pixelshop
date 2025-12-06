package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.repository.UsuarioRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioUsuarioImpl implements IServicioUsuario {
    
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    
    // Busca un usuario por su ID en la base de datos
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    // Busca un usuario por su dirección de correo electrónico
    // IMPORTANTE: Devuelve el usuario sin importar si está activo o inactivo
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    // Verifica si ya existe un usuario ACTIVO con el email proporcionado
    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailAndActivo(email, true);
    }
    
    // Guarda o actualiza un usuario en la base de datos
    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    // Registra un nuevo usuario, encripta su contraseña y lo activa
    @Override
    public Usuario registrar(Usuario usuario) {
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
    
    // Actualiza los datos de un usuario existente (nombre, email, contraseña, rol, etc.)
    @Override
    public Usuario actualizar(Usuario usuario) {
        // Validar que el usuario existe
        Usuario existente = usuarioRepository.findById(usuario.getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getId()));
        
        // Actualizar campos permitidos
        existente.setNombre(usuario.getNombre());
        existente.setEmail(usuario.getEmail());
        
        // Solo actualizar contraseña si se proporcionó una nueva
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            existente.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        existente.setRol(usuario.getRol());
        existente.setCifNif(usuario.getCifNif());
        existente.setEmailPaypal(usuario.getEmailPaypal());
        existente.setActivo(usuario.getActivo());
        
        return usuarioRepository.save(existente);
    }
    
    // Elimina un usuario de forma FÍSICA (borra completamente de la base de datos)
    // PRESERVA: Solo las transacciones de plataforma (para historial de ganancias)
    // ELIMINA: Usuario, compras, transacciones de proveedor
    // DESVINCULA: Juegos (quedan huérfanos pero los clientes conservan sus compras)
    @Override
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        String emailOriginal = usuario.getEmail();
        System.out.println(" Eliminando usuario ID " + id + " (" + emailOriginal + ")");
        
        // 1. DESVINCULAR transacciones de plataforma (se preservan para historial)
        if (!usuario.getTransaccionesPlataforma().isEmpty()) {
            System.out.println("  Preservando " + usuario.getTransaccionesPlataforma().size() + " transacciones de plataforma");
            for (var transaccion : usuario.getTransaccionesPlataforma()) {
                transaccion.setUsuario(null); // Desvincular usuario
            }
        }
        
        // 2. DESVINCULAR juegos publicados (quedan huérfanos pero activos)
        // Los clientes que compraron estos juegos los conservan
        if (!usuario.getJuegosPublicados().isEmpty()) {
            System.out.println("   Desvinculando " + usuario.getJuegosPublicados().size() + " juegos publicados");
            for (var juego : usuario.getJuegosPublicados()) {
                juego.setProveedor(null); // Desvincular proveedor
                System.out.println("      - " + juego.getTitulo() + " (los clientes conservan sus compras)");
            }
        }
        
        // 3. ELIMINAR compras del usuario (si es cliente)
        // Esto NO afecta a los juegos ni a las transacciones de plataforma
        if (!usuario.getCompras().isEmpty()) {
            System.out.println(" Eliminando " + usuario.getCompras().size() + " compras del usuario");
        }
        
        // 4. ELIMINAR transacciones de proveedor (si es proveedor)
        if (!usuario.getTransacciones().isEmpty()) {
            System.out.println(" Eliminando " + usuario.getTransacciones().size() + " transacciones de proveedor");
        }
        
        // 5. ELIMINAR físicamente el usuario
        usuarioRepository.delete(usuario);
        
        System.out.println("Usuario eliminado completamente de la base de datos");
        System.out.println(" Email liberado: " + emailOriginal + " (puede crear cuenta nueva)");
        System.out.println(" Transacciones de plataforma preservadas (historial de ganancias)");
        System.out.println(" Juegos publicados conservados (clientes mantienen sus compras)");
    }
    
    // Obtiene la lista completa de todos los usuarios ACTIVOS (excluye eliminados)
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findByActivo(true);
    }
    
    // Lista todos los usuarios ACTIVOS que tienen un rol específico (ADMIN, PROVEEDOR, CLIENTE)
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByActivoAndRol(true, rol);
    }
    
    // Obtiene la lista de todos los usuarios con rol PROVEEDOR
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarProveedores() {
        return usuarioRepository.findAllProveedores();
    }
    
    // Obtiene la lista de todos los usuarios con rol CLIENTE
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarClientes() {
        return usuarioRepository.findAllClientes();
    }
    
    // Cuenta el número total de usuarios registrados en el sistema
    @Override
    @Transactional(readOnly = true)
    public long contarTodos() {
        return usuarioRepository.count();
    }
    
    // Cuenta cuántos usuarios tienen un rol específico
    @Override
    @Transactional(readOnly = true)
    public long contarPorRol(Rol rol) {
        return usuarioRepository.countByRolActive(rol);
    }
    
    // Activa o desactiva un usuario (banearlo o desbanearlo)
    @Override
    public void cambiarEstado(Integer id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }
}
