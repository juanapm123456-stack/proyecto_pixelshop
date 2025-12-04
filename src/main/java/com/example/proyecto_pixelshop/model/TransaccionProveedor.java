package com.example.proyecto_pixelshop.model;

import com.example.proyecto_pixelshop.model.enums.EstadoPago;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion_proveedor")
public class TransaccionProveedor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // ⭐ Relación 1:1 con Compra (UNIQUE garantiza que solo hay 1 transacción por compra)
    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false, unique = true)
    private Compra compra;
    
    // ⭐ Relación con Usuario (proveedor)
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // ⭐ NO tiene juego_id - se obtiene desde compra.getJuego()
    
    @Column(name = "importe_bruto", nullable = false)
    private Double importeBruto;
    
    @Column(name = "porcentaje_comision", nullable = false)
    private Double porcentajeComision = 15.0;
    
    @Column(name = "importe_comision", nullable = false)
    private Double importeComision;
    
    @Column(name = "importe_neto", nullable = false)
    private Double importeNeto;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    
    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;
    
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    // Constructores
    public TransaccionProveedor() {}
    
    public TransaccionProveedor(Compra compra, Usuario usuario, Double importeBruto, Double porcentajeComision) {
        this.compra = compra;
        this.usuario = usuario;
        this.importeBruto = importeBruto;
        this.porcentajeComision = porcentajeComision;
        this.estadoPago = EstadoPago.PENDIENTE;
    }
    
    
    @PrePersist
    protected void onCreate() {
        this.fechaVenta = LocalDateTime.now();
        if (this.estadoPago == null) {
            this.estadoPago = EstadoPago.PENDIENTE;
        }
    }
    
    // Getters y Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Compra getCompra() {return compra;}
    public void setCompra(Compra compra) {this.compra = compra;}
    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}
    public Double getImporteBruto() {return importeBruto;}
    public void setImporteBruto(Double importeBruto) {this.importeBruto = importeBruto;}
    public Double getPorcentajeComision() {return porcentajeComision;}
    public void setPorcentajeComision(Double porcentajeComision) {this.porcentajeComision = porcentajeComision;}
    public Double getImporteComision() {return importeComision;}
    public void setImporteComision(Double importeComision) {this.importeComision = importeComision;}
    public Double getImporteNeto() {return importeNeto;}
    public void setImporteNeto(Double importeNeto) {this.importeNeto = importeNeto;}
    public EstadoPago getEstadoPago() {return estadoPago;}
    public void setEstadoPago(EstadoPago estadoPago) {this.estadoPago = estadoPago;}
    public LocalDateTime getFechaVenta() {return fechaVenta;}
    public void setFechaVenta(LocalDateTime fechaVenta) {this.fechaVenta = fechaVenta;}
    public LocalDateTime getFechaPago() {return fechaPago;}
    public void setFechaPago(LocalDateTime fechaPago) {this.fechaPago = fechaPago;}
}
