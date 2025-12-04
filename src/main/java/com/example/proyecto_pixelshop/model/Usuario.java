package com.example.proyecto_pixelshop.model;

import com.example.proyecto_pixelshop.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    
    // Campos de proveedor (NULL si no es proveedor)
    @Column(name = "cif_nif", length = 20)
    private String cifNif;
    
    @Column(name = "email_paypal", length = 100)
    private String emailPaypal;
    
    // Google OAuth
    @Column(name = "id_google", length = 100)
    private String idGoogle;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Juego> juegosPublicados = new ArrayList<>();
    
    // ⭐ Ahora apunta a "usuario" en lugar de "proveedor"
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<TransaccionProveedor> transacciones = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<TransaccionPlataforma> transaccionesPlataforma = new ArrayList<>();
    
    // Constructores
    public Usuario() {}
    
    public Usuario(String nombre, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public Rol getRol() {return rol;}
    public void setRol(Rol rol) {this.rol = rol;}
    public String getCifNif() {return cifNif;}
    public void setCifNif(String cifNif) {this.cifNif = cifNif;}
    public String getEmailPaypal() {return emailPaypal;}
    public void setEmailPaypal(String emailPaypal) {this.emailPaypal = emailPaypal;}
    public String getIdGoogle() {return idGoogle;}
    public void setIdGoogle(String idGoogle) {this.idGoogle = idGoogle;}
    public Boolean getActivo() {return activo;}
    public void setActivo(Boolean activo) {this.activo = activo;}
    public LocalDateTime getFechaRegistro() {return fechaRegistro;}
    public void setFechaRegistro(LocalDateTime fechaRegistro) {this.fechaRegistro = fechaRegistro;}
    public LocalDateTime getFechaActualizacion() {return fechaActualizacion;}
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {this.fechaActualizacion = fechaActualizacion;}
    public List<Compra> getCompras() {return compras;}
    public void setCompras(List<Compra> compras) {this.compras = compras;}
    public List<Juego> getJuegosPublicados() {return juegosPublicados;}
    public void setJuegosPublicados(List<Juego> juegosPublicados) {this.juegosPublicados = juegosPublicados;}
    public List<TransaccionProveedor> getTransacciones() {return transacciones;}
    public void setTransacciones(List<TransaccionProveedor> transacciones) {this.transacciones = transacciones;}
    public List<TransaccionPlataforma> getTransaccionesPlataforma() {return transaccionesPlataforma;}
    public void setTransaccionesPlataforma(List<TransaccionPlataforma> transaccionesPlataforma) {this.transaccionesPlataforma = transaccionesPlataforma;}
    
}
