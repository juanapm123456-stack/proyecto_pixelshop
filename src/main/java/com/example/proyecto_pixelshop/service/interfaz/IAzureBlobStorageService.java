package com.example.proyecto_pixelshop.service.interfaz;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// Interfaz para operaciones de Azure Blob Storage
public interface IAzureBlobStorageService {
    
    /**
     * Sube un archivo (imagen o video) a Azure Blob Storage
     * @param file Archivo a subir
     * @param folder Carpeta dentro del contenedor (ej: "juegos", "usuarios", "videos")
     * @return URL pública del archivo
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;
    
    /**
     * Sube múltiples archivos a Azure Blob Storage
     * @param files Array de archivos
     * @param folder Carpeta dentro del contenedor
     * @return Lista de URLs públicas
     */
    List<String> uploadMultipleFiles(MultipartFile[] files, String folder) throws IOException;
    
    /**
     * Elimina un archivo de Azure Blob Storage usando su URL
     * @param fileUrl URL del archivo a eliminar
     */
    void deleteFile(String fileUrl) throws IOException;
    
    /**
     * Elimina múltiples archivos de Azure Blob Storage
     * @param fileUrls Lista de URLs de archivos a eliminar
     */
    void deleteMultipleFiles(List<String> fileUrls);
    
    /**
     * Verifica si una URL es de Azure Blob Storage
     */
    boolean isAzureBlobUrl(String url);
    
    /**
     * Actualiza un archivo (elimina el viejo y sube el nuevo)
     * @param oldFileUrl URL del archivo anterior
     * @param newFile Nuevo archivo
     * @param folder Carpeta donde subir
     * @return URL del nuevo archivo
     */
    String updateFile(String oldFileUrl, MultipartFile newFile, String folder) throws IOException;
}
