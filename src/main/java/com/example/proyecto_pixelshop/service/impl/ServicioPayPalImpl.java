package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Juego;
import com.example.proyecto_pixelshop.service.interfaz.IServicioPayPal;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        // Configurar beneficiario (proveedor del juego) - El dinero va directo a su email de PayPal
        Payee payee = new Payee().email(juego.getProveedor().getEmailPaypal());
        
        // Configurar unidad de compra
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
            .referenceId(juego.getId().toString())
            .description("Compra de juego: " + juego.getTitulo())
            .amountWithBreakdown(amount)
            .items(items)
            .payee(payee);  // El dinero va directo al email del proveedor

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
}
