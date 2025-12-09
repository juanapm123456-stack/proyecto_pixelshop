# 🎮 PixelShop

Plataforma digital de compra y venta de videojuegos online desarrollada como **Trabajo de Fin de Grado (TFG)** del ciclo formativo **DAM** (Desarrollo de Aplicaciones Multiplataforma).

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## 📋 Descripción

PixelShop es una plataforma web 100% digital donde:
- **Clientes** pueden comprar juegos y acceder a su biblioteca digital
- **Proveedores** pueden publicar y vender sus juegos (pago de 25€ por publicación)
- **Administradores** gestionan la plataforma

### 💰 Modelo de Negocio
- Proveedores pagan **25€** por publicar un juego
- Por cada venta: **85%** para el proveedor, **15%** para la plataforma

---

## 🚀 Tecnologías

- **Backend**: Java 21, Spring Boot , Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript
- **Base de Datos**: MySQL 
- **Pagos**: PayPal SDK 
- **Autenticación**: OAuth2 (Google), BCrypt
- **Almacenamiento**: Azure Blob Storage 
- **Email**: Gmail SMTP

---

## ⚙️ Instalación

### Requisitos
- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Pasos

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/proyecto_pixelshop.git
cd proyecto_pixelshop
```

2. **Crear base de datos**
```sql
CREATE DATABASE pixelshop;
```

3. **Configurar variables de entorno**
```properties
DB_URL=jdbc:mysql://localhost:3306/pixelshop
DB_USERNAME=root
DB_PASSWORD=tu_password
EMAIL_USERNAME=tu_email@gmail.com
EMAIL_PASSWORD=tu_app_password
PAYPAL_CLIENT_ID=tu_paypal_client_id
PAYPAL_CLIENT_SECRET=tu_paypal_secret
GOOGLE_CLIENT_ID=tu_google_client_id
GOOGLE_SECRET=tu_google_secret
```

4. **Ejecutar**
```bash
mvn spring-boot:run
```

Accede a: `http://localhost:8080`

---

## � Funcionalidades

-  Registro y login (tradicional + Google OAuth2)
-  Catálogo de juegos con búsqueda
-  Compra con PayPal
-  Biblioteca digital de juegos
-  Panel de administración
-  Sistema de transacciones y comisiones
-  Emails automáticos
-  Subida de imágenes/videos a Azure

---

## 📂 Estructura

```
src/
├── main/
│   ├── java/com/example/proyecto_pixelshop/
│   │   ├── config/         # Configuración
│   │   ├── controller/     # Controladores
│   │   ├── model/          # Entidades
│   │   ├── repository/     # Repositorios
│   │   └── service/        # Lógica de negocio
│   └── resources/
│       ├── static/         # CSS, JS
│       ├── templates/      # Vistas Thymeleaf
│       └── application.properties
```

---

## ‍💻 Autor

**Juan**  
TFG - DAM 2025

---

## � Licencia

Proyecto educativo - TFG
