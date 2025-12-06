package com.example.proyecto_pixelshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "juego")
public class Juego {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // nullable=true para preservar juegos cuando se elimina el proveedor
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = true)
    private Usuario proveedor;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false)
    private Double precio;
    
    @Column(length = 50)
    private String genero;
    
    @Column(name = "video_promocional_url", length = 255)
    private String videoPromocionalUrl;
    
    // Imágenes (4 columnas fijas)
    @Column(name = "imagen_portada_url", nullable = false, length = 255)
    private String imagenPortadaUrl;
    
    @Column(name = "imagen_2_url", length = 255)
    private String imagen2Url;
    
    @Column(name = "imagen_3_url", length = 255)
    private String imagen3Url;
    
    @Column(name = "imagen_4_url", length = 255)
    private String imagen4Url;
    
    // Archivo descargable (ZIP)
    @Column(name = "archivo_descargable_url", length = 500)
    private String archivoDescargableUrl;
    
    @Column(name = "archivo_nombre", length = 255)
    private String archivoNombre;
    
    @Column(name = "archivo_tamanio_bytes")
    private Integer archivoTamanioBytes;
    
    // Publicación (el proveedor paga 25€)
    @Column(name = "monto_publicacion", nullable = false)
    private Double montoPublicacion = 25.00;
    
    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    //  Ya no tiene relación directa con TransaccionPlataforma
    // Las transacciones se obtienen desde las compras
    
    // Constructores
    public Juego() {}
    
    public Juego(Usuario proveedor, String titulo, String descripcion, Double precio, String genero) {
        this.proveedor = proveedor;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.genero = genero;
        this.montoPublicacion = 25.00;
        this.activo = true;
    }
    
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.fechaPublicacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.montoPublicacion == null) {
            this.montoPublicacion = 25.00;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Usuario getProveedor() {return proveedor;}
    public void setProveedor(Usuario proveedor) {this.proveedor = proveedor;}
    public String getTitulo() {return titulo;}
    public void setTitulo(String titulo) {this.titulo = titulo;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public Double getPrecio() {return precio;}
    public void setPrecio(Double precio) {this.precio = precio;}
    public String getGenero() {return genero;}
    public void setGenero(String genero) {this.genero = genero;}
    public String getVideoPromocionalUrl() {return videoPromocionalUrl;}
    public void setVideoPromocionalUrl(String videoPromocionalUrl) {this.videoPromocionalUrl = videoPromocionalUrl;}
    public String getImagenPortadaUrl() {return imagenPortadaUrl;}
    public void setImagenPortadaUrl(String imagenPortadaUrl) {this.imagenPortadaUrl = imagenPortadaUrl;}
    public String getImagen2Url() {return imagen2Url;}
    public void setImagen2Url(String imagen2Url) {this.imagen2Url = imagen2Url;}
    public String getImagen3Url() {return imagen3Url;}
    public void setImagen3Url(String imagen3Url) {this.imagen3Url = imagen3Url;}
    public String getImagen4Url() {return imagen4Url;}
    public void setImagen4Url(String imagen4Url) {this.imagen4Url = imagen4Url;}
    public String getArchivoDescargableUrl() {return archivoDescargableUrl;}
    public void setArchivoDescargableUrl(String archivoDescargableUrl) {this.archivoDescargableUrl = archivoDescargableUrl;}
    public String getArchivoNombre() {return archivoNombre;}
    public void setArchivoNombre(String archivoNombre) {this.archivoNombre = archivoNombre;}
    public Integer getArchivoTamanioBytes() {return archivoTamanioBytes;}
    public void setArchivoTamanioBytes(Integer archivoTamanioBytes) {this.archivoTamanioBytes = archivoTamanioBytes;}
    public Double getMontoPublicacion() {return montoPublicacion;}
    public void setMontoPublicacion(Double montoPublicacion) {this.montoPublicacion = montoPublicacion;}
    public LocalDateTime getFechaPublicacion() {return fechaPublicacion;}
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {this.fechaPublicacion = fechaPublicacion;}
    public Boolean getActivo() {return activo;}
    public void setActivo(Boolean activo) {this.activo = activo;}
    public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}
    public LocalDateTime getFechaActualizacion() {return fechaActualizacion;}
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {this.fechaActualizacion = fechaActualizacion;}
    public List<Compra> getCompras() {return compras;}
    public void setCompras(List<Compra> compras) {this.compras = compras;}
}
