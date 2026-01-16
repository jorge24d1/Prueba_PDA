package com.concesionario.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "prospectos")
public class Prospecto {

    @Id
    private String id;

    // Datos Personales
    private String nombre;
    private String apellido;
    private String cedula;
    private String correo;
    private String telefono;

    // Datos de Interés
    private String vehiculoInteres; // Modelo, ID o descripción
    private String origen; // "Presencial", "Web", "Teléfono"

    // Gestión del Asesor
    private String trabajadorId; // ID del asesor asignado
    private String estado = "Nuevo"; // Nuevo, Contactado, En Proceso, Venta, Perdido
    private String observaciones;

    // Fechas
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    private LocalDateTime ultimoContacto;

    public Prospecto() {
    }

    // Constructor para registro rápido
    public Prospecto(String nombre, String apellido, String telefono, String vehiculoInteres, String trabajadorId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.vehiculoInteres = vehiculoInteres;
        this.trabajadorId = trabajadorId;
        this.origen = "Presencial";
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getVehiculoInteres() { return vehiculoInteres; }
    public void setVehiculoInteres(String vehiculoInteres) { this.vehiculoInteres = vehiculoInteres; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getTrabajadorId() { return trabajadorId; }
    public void setTrabajadorId(String trabajadorId) { this.trabajadorId = trabajadorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getUltimoContacto() { return ultimoContacto; }
    public void setUltimoContacto(LocalDateTime ultimoContacto) { this.ultimoContacto = ultimoContacto; }
}
