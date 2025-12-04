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
    
    // Elimina un usuario de forma LÓGICA (marca como inactivo, NO borra físicamente)
    // - Preserva: compras, transacciones y juegos (para que los clientes conserven sus compras)
    // - Desactiva: juegos publicados (para que no aparezcan en el catálogo)
    // - Modifica: email (para liberar el email y permitir crear cuenta nueva)
    @Override
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        String emailOriginal = usuario.getEmail();
        System.out.println(" Eliminando usuario ID " + id + " (" + emailOriginal + ")");
        System.out.println("   - Compras a preservar: " + usuario.getCompras().size());
        System.out.println("   - Juegos a desactivar: " + usuario.getJuegosPublicados().size());
        
        // Desactivar los juegos publicados por el usuario (borrado lógico)
        // Esto permite que los clientes conserven sus compras
        if (!usuario.getJuegosPublicados().isEmpty()) {
            int cantidadJuegos = usuario.getJuegosPublicados().size();
            for (var juego : usuario.getJuegosPublicados()) {
                juego.setActivo(false);
                System.out.println("    Juego desactivado: " + juego.getTitulo());
            }
            System.out.println("    " + cantidadJuegos + " juegos desactivados (no aparecerán en el catálogo)");
        }
        
        // Modificar el email para liberar el email original
        // Formato: email_original_DELETED_timestamp_id
        String emailEliminado = emailOriginal + "_DELETED_" + System.currentTimeMillis() + "_" + id;
        usuario.setEmail(emailEliminado);
        
        // Borrado lógico del usuario: marcar como inactivo
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        
        System.out.println(" Usuario marcado como inactivo (borrado lógico)");
        System.out.println("  Email modificado: " + emailOriginal + " → " + emailEliminado);
        System.out.println("  Email liberado para crear cuenta nueva");
        System.out.println("  Compras y transacciones preservadas");
        System.out.println("  Los clientes conservan sus juegos comprados");
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
