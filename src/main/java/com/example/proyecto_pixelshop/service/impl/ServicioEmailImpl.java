package com.example.proyecto_pixelshop.service.impl;

import com.example.proyecto_pixelshop.model.Compra;
import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.service.interfaz.IServicioEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;

@Service
public class ServicioEmailImpl implements IServicioEmail {

    @Autowired private JavaMailSender mailSender;
    @Autowired private TemplateEngine templateEngine;

    @Value("${email.from}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Envía un email de bienvenida al usuario cuando se registra en la plataforma
    @Override
    public void enviarEmailBienvenida(Usuario usuario) {
        try {
            System.out.println("Construyendo email de bienvenida para: " + usuario.getEmail());
            
            Context context = new Context();
            context.setVariable("nombre", usuario.getNombre());
            String htmlContent = templateEngine.process("email/bienvenida", context);
            
            System.out.println("Enviando email via Gmail SMTP...");
            System.out.println("  From: " + fromEmail);
            System.out.println("  To: " + usuario.getEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Bienvenido a PixelShop");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Email de bienvenida enviado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al enviar email de bienvenida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Envía un email de confirmación al usuario cuando completa una compra
    @Override
    public void enviarConfirmacionCompra(Compra compra) {
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", compra.getUsuario().getNombre());
            context.setVariable("tituloJuego", compra.getJuego().getTitulo());
            context.setVariable("fecha", compra.getFechaCompra().format(DATE_FORMATTER));
            context.setVariable("precio", String.format("%.2f", compra.getPrecioPagado()));
            String htmlContent = templateEngine.process("email/confirmacion-compra", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(compra.getUsuario().getEmail());
            helper.setSubject("Confirmación de compra - " + compra.getJuego().getTitulo());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Email de confirmación enviado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al enviar confirmación de compra: " + e.getMessage());
        }
    }

    // Envía un email al proveedor notificándole que su juego fue vendido
    @Override
    public void enviarNotificacionVenta(Compra compra) {
        try {
            Usuario proveedor = compra.getJuego().getProveedor();
            Double gananciaProveedor = compra.getPrecioPagado() * 0.85;
            
            Context context = new Context();
            context.setVariable("nombreProveedor", proveedor.getNombre());
            context.setVariable("tituloJuego", compra.getJuego().getTitulo());
            context.setVariable("precioVenta", String.format("%.2f", compra.getPrecioPagado()));
            context.setVariable("gananciaProveedor", String.format("%.2f", gananciaProveedor));
            context.setVariable("fecha", compra.getFechaCompra().format(DATE_FORMATTER));
            String htmlContent = templateEngine.process("email/notificacion-venta", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(proveedor.getEmail());
            helper.setSubject("Nueva venta - " + compra.getJuego().getTitulo());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Email de notificación de venta enviado a proveedor");
            
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de venta: " + e.getMessage());
        }
    }

    // Envía un email de recuperación de contraseña con un token temporal (expira en 1 hora)
    public void enviarRecuperacionPassword(String email, String nombreUsuario, String token) {
        try {
            String enlaceRecuperacion = "http://localhost:8080/auth/reset-password?token=" + token;
            
            Context context = new Context();
            context.setVariable("nombreUsuario", nombreUsuario);
            context.setVariable("enlaceRecuperacion", enlaceRecuperacion);
            String htmlContent = templateEngine.process("email/recuperacion-password", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Recuperación de contraseña - PixelShop");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Email de recuperación enviado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al enviar email de recuperación: " + e.getMessage());
        }
    }

    // Envía un email al usuario con el número de seguimiento de su pedido
    public void enviarNotificacionEnvio(Compra compra, String numeroSeguimiento) {
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", compra.getUsuario().getNombre());
            context.setVariable("tituloJuego", compra.getJuego().getTitulo());
            context.setVariable("numeroSeguimiento", numeroSeguimiento);
            String htmlContent = templateEngine.process("email/notificacion-envio", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(compra.getUsuario().getEmail());
            helper.setSubject("Tu juego está en camino - " + compra.getJuego().getTitulo());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Email de notificación de envío enviado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de envío: " + e.getMessage());
        }
    }
}

