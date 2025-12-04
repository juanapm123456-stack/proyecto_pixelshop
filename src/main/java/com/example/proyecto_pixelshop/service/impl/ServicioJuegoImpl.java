package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.repository.JuegoRepository;
import com.example.proyecto_pixelshop.service.interfaz.IServicioJuego;
import com.example.proyecto_pixelshop.service.interfaz.IServicioTransaccionPlataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioJuegoImpl implements IServicioJuego {
    
    @Autowired private JuegoRepository juegoRepository;
    @Autowired private IServicioTransaccionPlataforma transaccionPlataformaService;
    
    // Busca un juego por su ID en la base de datos
    @Override
    @Transactional(readOnly = true)
    public Optional<Juego> buscarPorId(Integer id) {
        return juegoRepository.findById(id);
    }
    
    // Lista todos los juegos que están activos (publicados y visibles en el catálogo)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> listarActivos() {
        return juegoRepository.findByActivoTrue();
    }
    
    // Obtiene la lista completa de todos los juegos (activos e inactivos)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> listarTodos() {
        return juegoRepository.findAll();
    }
    
    // Lista todos los juegos de un proveedor específico (activos e inactivos)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> listarPorProveedor(Usuario proveedor) {
        return juegoRepository.findByProveedor(proveedor);
    }
    
    // Lista solo los juegos activos de un proveedor específico
    @Override
    @Transactional(readOnly = true)
    public List<Juego> listarActivosPorProveedor(Usuario proveedor) {
        return juegoRepository.findByProveedorAndActivoTrue(proveedor);
    }
    
    // Busca juegos activos cuyo título contenga el texto proporcionado (búsqueda flexible)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> buscarPorTitulo(String titulo) {
        return juegoRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo);
    }
    
    // Busca juegos activos por género (Acción, Aventura, RPG, etc.)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> buscarPorGenero(String genero) {
        return juegoRepository.findByGeneroAndActivoTrue(genero);
    }
    
    // Guarda o actualiza un juego en la base de datos
    @Override
    public Juego guardar(Juego juego) {
        // Validar que no exista otro juego ACTIVO con el mismo nombre
        validarNombreUnico(juego);
        return juegoRepository.save(juego);
    }
    
    // Publica un juego: establece fecha de publicación, activa el juego y registra el pago de 25€ a la plataforma
    @Override
    public Juego publicar(Juego juego) {
        // Validar que no exista otro juego ACTIVO con el mismo nombre
        validarNombreUnico(juego);
        
        // Establecer fecha de publicación y estado activo
        juego.setFechaPublicacion(LocalDateTime.now());
        juego.setActivo(true);
        
        // Establecer pago de publicación (25€)
        if (juego.getMontoPublicacion() == null) {
            juego.setMontoPublicacion(25.00);
        }
        
        // Guardar el juego
        Juego juegoGuardado = juegoRepository.save(juego);
        
        // Registrar transacción de plataforma por pago de publicación
        transaccionPlataformaService.registrarPagoPublicacion(
            juegoGuardado.getId(), 
            juegoGuardado.getMontoPublicacion()
        );
        
        return juegoGuardado;
    }
    
    // Actualiza los datos de un juego existente (título, descripción, precio, imágenes, etc.)
    @Override
    public Juego actualizar(Juego juego) {
        // Validar que el juego existe
        Juego existente = juegoRepository.findById(juego.getId())
            .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + juego.getId()));
        
        // Validar que no exista otro juego ACTIVO con el mismo nombre (si se cambió el título)
        if (!existente.getTitulo().equalsIgnoreCase(juego.getTitulo())) {
            validarNombreUnico(juego);
        }
        
        // Actualizar campos permitidos
        existente.setTitulo(juego.getTitulo());
        existente.setDescripcion(juego.getDescripcion());
        existente.setGenero(juego.getGenero());
        existente.setPrecio(juego.getPrecio());
        existente.setImagenPortadaUrl(juego.getImagenPortadaUrl());
        existente.setImagen2Url(juego.getImagen2Url());
        existente.setImagen3Url(juego.getImagen3Url());
        existente.setImagen4Url(juego.getImagen4Url());
        existente.setArchivoDescargableUrl(juego.getArchivoDescargableUrl());
        existente.setArchivoNombre(juego.getArchivoNombre());
        existente.setArchivoTamanioBytes(juego.getArchivoTamanioBytes());
        existente.setActivo(juego.getActivo());
        
        return juegoRepository.save(existente);
    }
    
    // Elimina un juego de la base de datos por su ID
    @Override
    public void eliminar(Integer id) {
        juegoRepository.deleteById(id);
    }
    
    // Activa o desactiva un juego (mostrarlo u ocultarlo del catálogo)
    @Override
    public void cambiarEstado(Integer id, Boolean activo) {
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + id));
        juego.setActivo(activo);
        juegoRepository.save(juego);
    }
    
    // Actualiza las URLs de las 4 imágenes de un juego (portada y capturas)
    @Override
    public void actualizarImagenes(Integer id, String imagen1, String imagen2, String imagen3, String imagen4) {
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + id));
        
        if (imagen1 != null) juego.setImagenPortadaUrl(imagen1);
        if (imagen2 != null) juego.setImagen2Url(imagen2);
        if (imagen3 != null) juego.setImagen3Url(imagen3);
        if (imagen4 != null) juego.setImagen4Url(imagen4);
        
        juegoRepository.save(juego);
    }
    
    // Actualiza la información del archivo ZIP del juego (URL, nombre y tamaño)
    @Override
    public void actualizarArchivo(Integer id, String archivoUrl, String archivoNombre, Long archivoTamanio) {
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + id));
        
        juego.setArchivoDescargableUrl(archivoUrl);
        juego.setArchivoNombre(archivoNombre);
        juego.setArchivoTamanioBytes(archivoTamanio != null ? archivoTamanio.intValue() : null);
        
        juegoRepository.save(juego);
    }
    
    // Calcula el total de pagos por publicación (25â‚¬ por juego) que ha realizado un proveedor
    @Override
    @Transactional(readOnly = true)
    public Double calcularPagosPublicacionProveedor(Usuario proveedor) {
        Double total = juegoRepository.calcularPagosPublicacionProveedor(proveedor);
        return total != null ? total : 0.0;
    }
    
    // Lista todos los juegos que han sido publicados (tienen fecha de publicación)
    @Override
    @Transactional(readOnly = true)
    public List<Juego> listarPublicados() {
        return juegoRepository.findAllPublicados();
    }
    
    // Valida que no exista otro juego ACTIVO con el mismo nombre (ignora mayúsculas/minúsculas)
    private void validarNombreUnico(Juego juego) {
        List<Juego> juegosActivos = juegoRepository.findByActivoTrue();
        
        for (Juego juegoExistente : juegosActivos) {
            // Si es el mismo juego (mismo ID), no validar
            if (juego.getId() != null && juegoExistente.getId().equals(juego.getId())) {
                continue;
            }
            
            // Si hay otro juego activo con el mismo nombre (ignorando mayúsculas)
            if (juegoExistente.getTitulo().equalsIgnoreCase(juego.getTitulo())) {
                throw new RuntimeException("Ya existe un juego activo con el nombre '" + juego.getTitulo() + "'. Por favor, elige otro nombre.");
            }
        }
    }
    
    // Cuenta cuántos juegos están activos en el sistema
    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return juegoRepository.countByActivoTrue();
    }
}
