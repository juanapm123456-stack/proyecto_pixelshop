package com.example.proyecto_pixelshop.model;

import com.example.proyecto_pixelshop.model.enums.TipoTransaccion;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion_plataforma")
public class TransaccionPlataforma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false)
    private TipoTransaccion tipoTransaccion;
    
    @Column(nullable = false)
    private Double importe;
    
    @Column(name = "porcentaje_comision")
    private Double porcentajeComision;
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;
    
    // RELACIONES
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    //  Relación 1:1 con Compra (SIEMPRE - incluso para PAGO_PUBLICACION si aplica)
    @OneToOne
    @JoinColumn(name = "compra_id", unique = true)
    private Compra compra;
    
    //  NO tiene juego_id - se obtiene desde compra.getJuego()
    
    // Constructores
    public TransaccionPlataforma() {}
    
    // Constructor para COMISION_VENTA (con Compra)
    public TransaccionPlataforma(TipoTransaccion tipoTransaccion, Double importe, Usuario usuario, Compra compra, String descripcion, Double porcentajeComision) {
        this.tipoTransaccion = tipoTransaccion;
        this.importe = importe;
        this.usuario = usuario;
        this.compra = compra;
        this.descripcion = descripcion;
        this.porcentajeComision = porcentajeComision;
    }
    
    // Constructor simplificado (para PAGO_PUBLICACION sin Compra)
    public TransaccionPlataforma(TipoTransaccion tipoTransaccion, Double importe, Usuario usuario, String descripcion) {
        this.tipoTransaccion = tipoTransaccion;
        this.importe = importe;
        this.usuario = usuario;
        this.descripcion = descripcion;
    }
    
    
    @PrePersist
    protected void onCreate() {
        this.fechaTransaccion = LocalDateTime.now();
        validarRelaciones();
    }
    
    @PreUpdate
    protected void onUpdate() {
        validarRelaciones();
    }
    
    //  Validación: COMISION_VENTA requiere Compra
    private void validarRelaciones() {
        if (tipoTransaccion == TipoTransaccion.COMISION_VENTA) {
            if (compra == null) {
                throw new IllegalStateException("COMISION_VENTA requiere una Compra asociada");
            }
        }
        // PAGO_PUBLICACION puede o no tener compra (es opcional)
    }
    
    // Getters y Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public TipoTransaccion getTipoTransaccion() {return tipoTransaccion;}
    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {this.tipoTransaccion = tipoTransaccion;}
    public Double getImporte() {return importe;}
    public void setImporte(Double importe) {this.importe = importe;}
    public Double getPorcentajeComision() {return porcentajeComision;}
    public void setPorcentajeComision(Double porcentajeComision) {this.porcentajeComision = porcentajeComision;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public LocalDateTime getFechaTransaccion() {return fechaTransaccion;}
    public void setFechaTransaccion(LocalDateTime fechaTransaccion) {this.fechaTransaccion = fechaTransaccion;}
    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}
    public Compra getCompra() {return compra;}
    public void setCompra(Compra compra) {this.compra = compra;}
}
