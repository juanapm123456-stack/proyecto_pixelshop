package com.example.proyecto_pixelshop.config.external;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Esta clase se encarga de configurar la conexión con la API de PayPal.
 * Inicializa el cliente HTTP de PayPal con las credenciales (client-id y client-secret)
 * y define el entorno (sandbox para desarrollo o live para producción).
 */
@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        System.out.println("=== Configurando PayPal Client ===");
        System.out.println("Client ID: " + (clientId != null ? clientId.substring(0, Math.min(20, clientId.length())) + "..." : "NULL"));
        System.out.println("Client Secret: " + (clientSecret != null ? "Configurado (" + clientSecret.length() + " caracteres)" : "NULL"));
        System.out.println("Modo: " + mode);
        
        PayPalEnvironment environment;
        
        // Configurar entorno según el modo
        if ("sandbox".equalsIgnoreCase(mode)) {
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
            System.out.println("Usando entorno SANDBOX");
        } else {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
            System.out.println("Usando entorno LIVE");
        }
        
        return new PayPalHttpClient(environment);
    }
}
