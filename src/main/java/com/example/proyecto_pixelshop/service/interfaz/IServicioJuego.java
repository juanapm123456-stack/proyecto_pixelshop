package com.example.proyecto_pixelshop.service.interfaz;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.model.Usuario;

import java.util.List;
import java.util.Optional;

// Interfaz de servicio para gestionar juegos
public interface IServicioJuego {
    
    // Busca un juego por su ID
    Optional<Juego> buscarPorId(Integer id);
    
    // Lista todos los juegos activos
    List<Juego> listarActivos();
    
    // Lista todos los juegos (activos e inactivos)
    List<Juego> listarTodos();
    
    // Lista juegos de un proveedor específico
    List<Juego> listarPorProveedor(Usuario proveedor);
    
    // Lista juegos activos de un proveedor
    List<Juego> listarActivosPorProveedor(Usuario proveedor);
    
    // Busca juegos por título (búsqueda parcial)
    List<Juego> buscarPorTitulo(String titulo);
    
    // Busca juegos activos por género
    List<Juego> buscarPorGenero(String genero);
    
    // Guarda o actualiza un juego
    Juego guardar(Juego juego);
    
    // Publica un nuevo juego (incluye pago de publicación de 25€)
    Juego publicar(Juego juego);
    
    // Actualiza un juego existente
    Juego actualizar(Juego juego);
    
    // Elimina un juego por su ID
    void eliminar(Integer id);
    
    // Activa o desactiva un juego
    void cambiarEstado(Integer id, Boolean activo);
    
    // Actualiza las imágenes de un juego
    void actualizarImagenes(Integer id, String imagen1, String imagen2, String imagen3, String imagen4);
    
    // Actualiza el archivo ZIP del juego
    void actualizarArchivo(Integer id, String archivoUrl, String archivoNombre, Long archivoTamanio);
    
    // Calcula el total de pagos de publicación de un proveedor
    Double calcularPagosPublicacionProveedor(Usuario proveedor);
    
    // Lista todos los juegos publicados ordenados por fecha
    List<Juego> listarPublicados();
    
    // Cuenta el total de juegos activos
    long contarActivos();
}
