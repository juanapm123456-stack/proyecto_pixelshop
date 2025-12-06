package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.repository.UsuarioRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    @PersistenceContext private EntityManager entityManager;
    
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
    // DESACTIVA Y DESVINCULA: Juegos (los clientes conservan sus compras)
    @Override
    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        String emailOriginal = usuario.getEmail();
        System.out.println(" Eliminando usuario ID " + id + " (" + emailOriginal + ")");
        
        // Contar elementos antes de eliminar
        int transaccionesPlataforma = usuario.getTransaccionesPlataforma().size();
        int juegosPublicados = usuario.getJuegosPublicados().size();
        int compras = usuario.getCompras().size();
        int transaccionesProveedor = usuario.getTransacciones().size();
        
        // PASO 1: ELIMINAR transacciones de proveedor del usuario (ganancias del proveedor)
        if (transaccionesProveedor > 0) {
            System.out.println(" Eliminando " + transaccionesProveedor + " transacciones de proveedor");
            entityManager.createQuery("DELETE FROM TransaccionProveedor t WHERE t.usuario.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
        }
        
        // PASO 2: ELIMINAR transacciones de proveedor de las compras del usuario (si es cliente)
        // Estas son las ganancias que otros proveedores obtuvieron de este cliente
        int transaccionesProveedorCompras = entityManager.createQuery(
            "SELECT COUNT(tp) FROM TransaccionProveedor tp WHERE tp.compra.usuario.id = :usuarioId", Long.class)
            .setParameter("usuarioId", id)
            .getSingleResult().intValue();
        
        if (transaccionesProveedorCompras > 0) {
            System.out.println(" Eliminando " + transaccionesProveedorCompras + " transacciones de proveedor de compras");
            entityManager.createQuery("DELETE FROM TransaccionProveedor tp WHERE tp.compra.usuario.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
        }
        
        // PASO 3: DESVINCULAR transacciones de plataforma de las compras del usuario (se preservan)
        int transaccionesPlataformaCompras = entityManager.createQuery(
            "SELECT COUNT(tp) FROM TransaccionPlataforma tp WHERE tp.compra.usuario.id = :usuarioId", Long.class)
            .setParameter("usuarioId", id)
            .getSingleResult().intValue();
        
        if (transaccionesPlataformaCompras > 0) {
            System.out.println(" Preservando " + transaccionesPlataformaCompras + " transacciones de plataforma de compras");
            entityManager.createQuery("UPDATE TransaccionPlataforma tp SET tp.usuario = null, tp.compra = null WHERE tp.compra.usuario.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
        }
        
        // PASO 4: DESVINCULAR transacciones de plataforma del usuario (se preservan)
        if (transaccionesPlataforma > 0) {
            System.out.println("  Preservando " + transaccionesPlataforma + " transacciones de plataforma del usuario");
            entityManager.createQuery("UPDATE TransaccionPlataforma t SET t.usuario = null WHERE t.usuario.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
        }
        
        // PASO 5: ELIMINAR compras del usuario (ahora sin transacciones asociadas)
        if (compras > 0) {
            System.out.println(" Eliminando " + compras + " compras del usuario");
            entityManager.createQuery("DELETE FROM Compra c WHERE c.usuario.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
        }
        
        // PASO 4: DESACTIVAR Y DESVINCULAR juegos publicados
        if (juegosPublicados > 0) {
            System.out.println("  Desactivando " + juegosPublicados + " juegos publicados");
            entityManager.createQuery("UPDATE Juego j SET j.activo = false, j.proveedor = null WHERE j.proveedor.id = :usuarioId")
                .setParameter("usuarioId", id)
                .executeUpdate();
            System.out.println(" Juegos desactivados (clientes conservan sus compras)");
        }
        
        // PASO 5: Limpiar el contexto de persistencia
        entityManager.flush();
        entityManager.clear();
        
        // PASO 6: ELIMINAR físicamente el usuario
        entityManager.createQuery("DELETE FROM Usuario u WHERE u.id = :usuarioId")
            .setParameter("usuarioId", id)
            .executeUpdate();
        
        System.out.println(" Usuario eliminado completamente de la base de datos");
        System.out.println("  Email liberado: " + emailOriginal + " (puede crear cuenta nueva)");
        System.out.println("  Transacciones de plataforma preservadas (historial de ganancias)");
        System.out.println(" Juegos desactivados (no aparecen en catálogo)");
        System.out.println(" Clientes conservan sus juegos comprados en biblioteca");
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
