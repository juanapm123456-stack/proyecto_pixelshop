package com.example.proyecto_pixelshop.config.external;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Azure Blob Storage para almacenar imágenes, videos y archivos.
 * Azure Blob Storage permite guardar cualquier tipo de archivo (imágenes, videos, PDFs, etc.)
 * y acceder a ellos mediante URLs públicas.
 */
@Configuration
public class AzureBlobStorageConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    
     //Crea el cliente de Azure Blob Service
     
    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    
     //Crea el cliente del contenedor donde se guardarán los archivos
    
    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        // Crear el contenedor si no existe
        if (!containerClient.exists()) {
            containerClient.create();
            System.out.println("Contenedor '" + containerName + "' creado exitosamente");
        }
        
        return containerClient;
    }
}
