package com.pizzeria.reparto.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String cliente;

    @NotBlank
    private String telefono;

    @NotBlank
    private String direccion;

    @Column(length = 1000)
    private String detallePedido;

    @Enumerated(EnumType.STRING)
    private OrigenPedido origen;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private Double distanciaKm;

    private Double latitud;
    private Double longitud;

    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidorAsignado;

    private String proveedorExterno;

    private String idEnvioExterno;
    private Integer tiempoEstimadoExternoMinutos;

    @Enumerated(EnumType.STRING)
    private EstadoEnvioExterno estadoEnvioExterno;

    private String nombreRepartidorExterno;

    private LocalDateTime fechaSolicitudExterno;

    public Long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDetallePedido() {
        return detallePedido;
    }

    public void setDetallePedido(String detallePedido) {
        this.detallePedido = detallePedido;
    }

    public OrigenPedido getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenPedido origen) {
        this.origen = origen;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Repartidor getRepartidorAsignado() {
        return repartidorAsignado;
    }

    public void setRepartidorAsignado(Repartidor repartidorAsignado) {
        this.repartidorAsignado = repartidorAsignado;
    }

    public String getProveedorExterno() {
        return proveedorExterno;
    }

    public void setProveedorExterno(String proveedorExterno) {
        this.proveedorExterno = proveedorExterno;
    }

    public String getIdEnvioExterno() {
        return idEnvioExterno;
    }

    public void setIdEnvioExterno(String idEnvioExterno) {
        this.idEnvioExterno = idEnvioExterno;
    }

    public Integer getTiempoEstimadoExternoMinutos() {
        return tiempoEstimadoExternoMinutos;
    }

    public void setTiempoEstimadoExternoMinutos(Integer tiempoEstimadoExternoMinutos) {
        this.tiempoEstimadoExternoMinutos = tiempoEstimadoExternoMinutos;
    }

    public EstadoEnvioExterno getEstadoEnvioExterno() {
        return estadoEnvioExterno;
    }

    public void setEstadoEnvioExterno(EstadoEnvioExterno estadoEnvioExterno) {
        this.estadoEnvioExterno = estadoEnvioExterno;
    }

    public String getNombreRepartidorExterno() {
        return nombreRepartidorExterno;
    }

    public void setNombreRepartidorExterno(String nombreRepartidorExterno) {
        this.nombreRepartidorExterno = nombreRepartidorExterno;
    }

    public LocalDateTime getFechaSolicitudExterno() {
        return fechaSolicitudExterno;
    }

    public void setFechaSolicitudExterno(LocalDateTime fechaSolicitudExterno) {
        this.fechaSolicitudExterno = fechaSolicitudExterno;
    }
}