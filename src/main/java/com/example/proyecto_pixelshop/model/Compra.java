package com.example.proyecto_pixelshop.model;

import com.example.proyecto_pixelshop.model.enums.EstadoCompra;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_juego", columnNames = {"usuario_id", "juego_id"})
})
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;
    
    @Column(name = "precio_pagado", nullable = false)
    private Double precioPagado;
    
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago = "Pago Online";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;
    
    @Column(name = "id_orden_paypal", length = 100)
    private String idOrdenPaypal;
    
    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;
    
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    // Relaciones
    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private TransaccionProveedor transaccionProveedor;
    
    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private TransaccionPlataforma transaccionPlataforma;
    
    // Constructores
    public Compra() {}
    
    public Compra(Usuario usuario, Juego juego, Double precioPagado) {
        this.usuario = usuario;
        this.juego = juego;
        this.precioPagado = precioPagado;
        this.metodoPago = "Pago Online";
        this.estado = EstadoCompra.PENDIENTE;
    }
    
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaCompra = LocalDateTime.now();
        if (this.metodoPago == null) {
            this.metodoPago = "Pago Online";
        }
        if (this.estado == null) {
            this.estado = EstadoCompra.PENDIENTE;
        }
    }
    
    // Getters y Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}
    public Juego getJuego() {return juego;}
    public void setJuego(Juego juego) {this.juego = juego;}
    public Double getPrecioPagado() {return precioPagado;}
    public void setPrecioPagado(Double precioPagado) {this.precioPagado = precioPagado;}
    public String getMetodoPago() {return metodoPago;}
    public void setMetodoPago(String metodoPago) {this.metodoPago = metodoPago;}
    public EstadoCompra getEstado() {return estado;}
    public void setEstado(EstadoCompra estado) {this.estado = estado;}
    public String getIdOrdenPaypal() {return idOrdenPaypal;}
    public void setIdOrdenPaypal(String idOrdenPaypal) {this.idOrdenPaypal = idOrdenPaypal;}
    public LocalDateTime getFechaCompra() {return fechaCompra;}
    public void setFechaCompra(LocalDateTime fechaCompra) {this.fechaCompra = fechaCompra;}
    public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}
    public TransaccionProveedor getTransaccionProveedor() {return transaccionProveedor;}
    public void setTransaccionProveedor(TransaccionProveedor transaccionProveedor) {this.transaccionProveedor = transaccionProveedor;}
    public TransaccionPlataforma getTransaccionPlataforma() {return transaccionPlataforma;}
    public void setTransaccionPlataforma(TransaccionPlataforma transaccionPlataforma) {this.transaccionPlataforma = transaccionPlataforma;}
}
