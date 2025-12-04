package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.service.interfaz.IServicioPayPal;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
public class ServicioPayPalImpl implements IServicioPayPal {

    @Autowired private PayPalHttpClient payPalHttpClient;

    // Crea una orden de pago en PayPal para un juego y retorna el ID de la orden
    @Override
    public String crearOrden(Juego juego, String returnUrl, String cancelUrl) throws IOException {
        // Configurar detalles del monto con breakdown
        AmountBreakdown breakdown = new AmountBreakdown()
            .itemTotal(new Money().currencyCode("EUR").value(juego.getPrecio().toString()));
        
        AmountWithBreakdown amount = new AmountWithBreakdown()
            .currencyCode("EUR")
            .value(juego.getPrecio().toString())
            .amountBreakdown(breakdown);

        // Configurar item de compra
        Item item = new Item()
            .name(juego.getTitulo())
            .description(juego.getDescripcion() != null ? 
                juego.getDescripcion().substring(0, Math.min(127, juego.getDescripcion().length())) : 
                "Juego digital")
            .unitAmount(new Money().currencyCode("EUR").value(juego.getPrecio().toString()))
            .quantity("1")
            .category("DIGITAL_GOODS");

        // Lista de items
        List<Item> items = new ArrayList<>();
        items.add(item);

        // Configurar unidad de compra
        // NO se especifica Payee, por lo que el dinero va a la cuenta de la plataforma
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
            .referenceId(juego.getId().toString())
            .description("Compra de juego: " + juego.getTitulo())
            .amountWithBreakdown(amount)
            .items(items);  // El dinero va a la plataforma, no al proveedor

        // Configurar contexto de aplicación
        ApplicationContext applicationContext = new ApplicationContext()
            .returnUrl(returnUrl)
            .cancelUrl(cancelUrl)
            .brandName("PixelShop")
            .landingPage("BILLING")
            .shippingPreference("NO_SHIPPING")
            .userAction("PAY_NOW");

        // Crear request de orden
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        orderRequest.applicationContext(applicationContext);
        orderRequest.purchaseUnits(List.of(purchaseUnit));

        // Ejecutar request
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.requestBody(orderRequest);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();
            
            return order.id();
            
        } catch (IOException e) {
            throw new IOException("Error al crear orden en PayPal: " + e.getMessage());
        }
    }

    // Captura el pago de una orden de PayPal (confirma la transacción)
    @Override
    public Order capturarPago(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        
        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();
            
        } catch (IOException e) {
            throw new IOException("Error al capturar pago en PayPal: " + e.getMessage());
        }
    }

    // Obtiene los detalles completos de una orden de PayPal por su ID
    @Override
    public Order obtenerDetallesOrden(String orderId) throws IOException {
        OrdersGetRequest request = new OrdersGetRequest(orderId);
        
        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();
            
        } catch (IOException e) {
            throw new IOException("Error al obtener detalles de la orden: " + e.getMessage());
        }
    }

    // Verifica si el pago de una orden de PayPal fue completado exitosamente
    @Override
    public boolean esPaymentCompletado(Order order) {
        if (order == null) return false;
        
        String status = order.status();
        return "COMPLETED".equals(status);
    }

    // Extrae el monto total pagado de una orden de PayPal
    @Override
    public Double extraerMontoPagado(Order order) {
        if (order == null || order.purchaseUnits() == null || order.purchaseUnits().isEmpty()) {
            return 0.0;
        }
        
        PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
        if (purchaseUnit.amountWithBreakdown() != null) {
            String value = purchaseUnit.amountWithBreakdown().value();
            return Double.parseDouble(value);
        }
        
        return 0.0;
    }

    // Extrae el ID del juego desde el reference_id de la orden de PayPal
    @Override
    public Integer extraerJuegoId(Order order) {
        if (order == null || order.purchaseUnits() == null || order.purchaseUnits().isEmpty()) {
            return null;
        }
        
        PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
        String referenceId = purchaseUnit.referenceId();
        
        try {
            return Integer.parseInt(referenceId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Value("${paypal.client-id}")
    private String clientId;
    
    @Value("${paypal.client-secret}")
    private String clientSecret;
    
    @Value("${paypal.mode}")
    private String mode;
    
    // Envía un pago (payout) a un proveedor mediante PayPal Payouts API
    @Override
    public String enviarPagoProveedor(String emailPaypal, Double monto, String descripcion) throws IOException {
        // Validar monto
        if (monto == null || monto <= 0) {
            throw new IOException("El monto debe ser mayor que 0");
        }
        
        // Redondear a 2 decimales para evitar problemas de precisión
        monto = Math.round(monto * 100.0) / 100.0;
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 1. Obtener token de acceso
            String accessToken = obtenerAccessToken(restTemplate);
            
            // 2. Crear payout
            String payoutUrl = mode.equals("sandbox") 
                ? "https://api-m.sandbox.paypal.com/v1/payments/payouts"
                : "https://api-m.paypal.com/v1/payments/payouts";
            
            // Crear JSON del payout
            Map<String, Object> payoutRequest = new HashMap<>();
            Map<String, String> senderBatchHeader = new HashMap<>();
            senderBatchHeader.put("sender_batch_id", "Payout_" + System.currentTimeMillis());
            senderBatchHeader.put("email_subject", "Has recibido un pago de PixelShop");
            senderBatchHeader.put("email_message", descripcion);
            
            Map<String, Object> item = new HashMap<>();
            item.put("recipient_type", "EMAIL");
            item.put("receiver", emailPaypal);
            Map<String, String> amount = new HashMap<>();
            // Formatear monto con exactamente 2 decimales usando punto como separador
            String montoFormateado = String.format(java.util.Locale.US, "%.2f", monto);
            amount.put("value", montoFormateado);
            amount.put("currency", "EUR");
            item.put("amount", amount);
            item.put("note", descripcion);
            item.put("sender_item_id", "item_" + System.currentTimeMillis());
            
            payoutRequest.put("sender_batch_header", senderBatchHeader);
            payoutRequest.put("items", List.of(item));
            
            // Log para depuración
            System.out.println("📤 Enviando payout a PayPal:");
            System.out.println("   Email: " + emailPaypal);
            System.out.println("   Monto: " + montoFormateado + " EUR");
            System.out.println("   Descripción: " + descripcion);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payoutRequest, headers);
            
            // Enviar payout
            ResponseEntity<Map> response = restTemplate.postForEntity(payoutUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("batch_header")) {
                    Map<String, Object> batchHeader = (Map<String, Object>) responseBody.get("batch_header");
                    return (String) batchHeader.get("payout_batch_id");
                }
            }
            
            throw new IOException("Error al crear payout: " + response.getStatusCode());
            
        } catch (Exception e) {
            throw new IOException("Error al enviar pago a proveedor: " + e.getMessage(), e);
        }
    }
    
    // Obtiene un token de acceso de PayPal
    private String obtenerAccessToken(RestTemplate restTemplate) throws IOException {
        try {
            String authUrl = mode.equals("sandbox")
                ? "https://api-m.sandbox.paypal.com/v1/oauth2/token"
                : "https://api-m.paypal.com/v1/oauth2/token";
            
            // Crear credenciales en Base64
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            String body = "grant_type=client_credentials";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
            
            throw new IOException("No se pudo obtener token de acceso");
            
        } catch (Exception e) {
            throw new IOException("Error al obtener token de PayPal: " + e.getMessage(), e);
        }
    }
}
