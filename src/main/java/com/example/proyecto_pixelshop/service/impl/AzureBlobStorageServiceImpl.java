package com.example.proyecto_pixelshop.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.example.proyecto_pixelshop.service.interfaz.IAzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AzureBlobStorageServiceImpl implements IAzureBlobStorageService {

    @Autowired private BlobContainerClient blobContainerClient;

    @Value("${azure.storage.blob-endpoint}")
    private String blobEndpoint;

    @Value("${azure.storage.container-name}")
    private String containerName;

    // Tipos de archivo permitidos
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/mpeg", "video/webm", "video/quicktime"
    );
    
    private static final List<String> ALLOWED_ARCHIVE_TYPES = Arrays.asList(
        "application/zip", "application/x-zip-compressed", "application/x-rar-compressed", 
        "application/x-7z-compressed", "application/octet-stream"
    );

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (!isValidFileType(contentType)) {
            throw new IOException("Tipo de archivo no permitido: " + contentType);
        }

        // Validar tamaño según tipo de archivo
        // Videos: máx 100MB, Archivos ZIP: máx 500MB, Imágenes: máx 10MB
        long maxSize;
        if (contentType.startsWith("video/")) {
            maxSize = 100 * 1024 * 1024; // 100MB
        } else if (ALLOWED_ARCHIVE_TYPES.contains(contentType)) {
            maxSize = 500L * 1024 * 1024; // 500MB
        } else {
            maxSize = 10 * 1024 * 1024; // 10MB
        }
        
        if (file.getSize() > maxSize) {
            throw new IOException("El archivo excede el tamaño máximo permitido (" + (maxSize / 1024 / 1024) + "MB)");
        }

        try {
            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

            // Obtener el cliente del blob
            BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

            // Configurar headers HTTP para el blob
            BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(contentType)
                .setContentDisposition("inline"); // Para que se muestre en el navegador

            // Subir el archivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            // Retornar la URL pública (decodificada para que funcione en <img src>)
            String encodedUrl = blobClient.getBlobUrl();
            // Decodificar solo el path, no toda la URL
            String decodedUrl = encodedUrl.replace("%2F", "/");
            return decodedUrl;

        } catch (Exception e) {
            throw new IOException("Error al subir el archivo a Azure: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> uploadMultipleFiles(MultipartFile[] files, String folder) throws IOException {
        List<String> urls = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            return urls;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    String url = uploadFile(file, folder);
                    urls.add(url);
                } catch (IOException e) {
                    System.err.println("Error al subir archivo: " + e.getMessage());
                    // Continuar con los demás archivos
                }
            }
        }

        return urls;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !isAzureBlobUrl(fileUrl)) {
            return;
        }

        try {
            // Extraer el nombre del blob de la URL
            String blobName = extractBlobNameFromUrl(fileUrl);
            
            if (blobName != null) {
                BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
                
                if (blobClient.exists()) {
                    blobClient.delete();
                    System.out.println("Archivo eliminado de Azure: " + blobName);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error al eliminar el archivo de Azure: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMultipleFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        for (String url : fileUrls) {
            if (url != null && !url.isEmpty()) {
                try {
                    deleteFile(url);
                } catch (IOException e) {
                    System.err.println("Error al eliminar archivo: " + e.getMessage());
                    // Continuar con los demás archivos
                }
            }
        }
    }

    @Override
    public boolean isAzureBlobUrl(String url) {
        return url != null && url.contains("blob.core.windows.net");
    }

    @Override
    public String updateFile(String oldFileUrl, MultipartFile newFile, String folder) throws IOException {
        System.out.println("=== updateFile ===");
        System.out.println("oldFileUrl: " + oldFileUrl);
        System.out.println("newFile: " + (newFile != null ? newFile.getOriginalFilename() : "null"));
        System.out.println("folder: " + folder);
        
        // Si hay archivo nuevo, subir
        String newUrl = null;
        if (newFile != null && !newFile.isEmpty()) {
            System.out.println("Subiendo nuevo archivo...");
            newUrl = uploadFile(newFile, folder);
            System.out.println("Archivo subido. Nueva URL: " + newUrl);
        }

        // Si se subió correctamente, eliminar el viejo
        if (newUrl != null && oldFileUrl != null && !oldFileUrl.isEmpty()) {
            try {
                System.out.println("Eliminando archivo anterior: " + oldFileUrl);
                deleteFile(oldFileUrl);
            } catch (IOException e) {
                System.err.println("Advertencia: No se pudo eliminar el archivo anterior: " + e.getMessage());
                // No fallar si no se puede eliminar el viejo
            }
        }

        System.out.println("=== updateFile retorna: " + newUrl + " ===");
        return newUrl;
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Verifica si un tipo de archivo es válido
     */
    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return ALLOWED_IMAGE_TYPES.contains(contentType) 
            || ALLOWED_VIDEO_TYPES.contains(contentType)
            || ALLOWED_ARCHIVE_TYPES.contains(contentType);
    }

    /**
     * Extrae el nombre del blob de una URL de Azure
     * Ejemplo: https://cuenta.blob.core.windows.net/contenedor/carpeta/archivo.jpg -> carpeta/archivo.jpg
     */
    private String extractBlobNameFromUrl(String fileUrl) {
        try {
            // Decodificar la URL primero para manejar caracteres como %2F
            String decodedUrl = java.net.URLDecoder.decode(fileUrl, "UTF-8");
            
            // Formato: https://[cuenta].blob.core.windows.net/[contenedor]/[nombre-blob]
            String[] parts = decodedUrl.split("/" + containerName + "/");
            if (parts.length > 1) {
                return parts[1].split("\\?")[0]; // Eliminar query params si los hay
            }
        } catch (Exception e) {
            System.err.println("Error al extraer nombre del blob: " + e.getMessage());
        }
        return null;
    }
}
