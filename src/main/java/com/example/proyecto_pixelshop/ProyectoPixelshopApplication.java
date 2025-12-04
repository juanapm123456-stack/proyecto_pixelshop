package com.example.proyecto_pixelshop;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.model.enums.Rol;
import com.example.proyecto_pixelshop.service.interfaz.IServicioUsuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProyectoPixelshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoPixelshopApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(IServicioUsuario usuarioService) {
		return args -> {
			// TEMPORAL: Eliminar admin existente para recrearlo con contraseña encriptada
			if (usuarioService.existeEmail("admin@pixelshop.com")) {
				System.out.println("⚠️  Eliminando admin existente para recrearlo...");
				Usuario adminViejo = usuarioService.buscarPorEmail("admin@pixelshop.com").orElse(null);
				if (adminViejo != null) {
					usuarioService.eliminar(adminViejo.getId());
					System.out.println("✅ Admin eliminado");
				}
			}
			
			// Crear usuario ADMIN
			Usuario admin = new Usuario();
			admin.setEmail("admin@pixelshop.com");
			admin.setPassword("admin123"); // El servicio lo encriptará automáticamente
			admin.setNombre("Administrador");
			admin.setRol(Rol.ADMIN);
			admin.setActivo(true);
			admin.setEmailPaypal("pixelshop@business.example.com"); // Email PayPal de la plataforma
			
			usuarioService.registrar(admin);
			System.out.println("✅ Usuario ADMIN creado exitosamente");
			System.out.println("   Email: admin@pixelshop.com");
			System.out.println("   Contraseña: admin123");
			System.out.println("   PayPal Plataforma: pixelshop@business.example.com");
		};
	}
}
