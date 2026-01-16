package com.concesionario.dto;

import java.time.LocalDateTime;

public class ProspectoDTO {
    private String id; // ID del Prospecto (antes citaId)
    private String usuarioId; // Opcional, si está registrado
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String vehiculoInteres;
    private String estado;
    private LocalDateTime ultimoContacto;
    private String origen; // Nuevo campo
    private String observaciones;

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public ProspectoDTO() {
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getVehiculoInteres() { return vehiculoInteres; }
    public void setVehiculoInteres(String vehiculoInteres) { this.vehiculoInteres = vehiculoInteres; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getUltimoContacto() { return ultimoContacto; }
    public void setUltimoContacto(LocalDateTime ultimoContacto) { this.ultimoContacto = ultimoContacto; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}