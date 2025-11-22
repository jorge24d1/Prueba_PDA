package com.concesionario.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "administradores")
public class Administrador {

    @Id
    private String id;

    private String nombreAdmin;
    private String apellidoAdmin;

    @Indexed(unique = true)
    private String identificacionAdmin;

    @Indexed(unique = true)
    private String correoAdmin;

    private String passwordAdmin;
    private Rol rol = Rol.ADMINISTRADOR;

    // Campos para recuperación de contraseña
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;

    // Campos de auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean activo = true;

    // Información de contacto adicional
    private String telefono;
    private String direccion;

    // Constructores
    public Administrador() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Administrador(String nombreAdmin, String apellidoAdmin, String identificacionAdmin,
                         String correoAdmin, String passwordAdmin) {
        this();
        this.nombreAdmin = nombreAdmin;
        this.apellidoAdmin = apellidoAdmin;
        this.identificacionAdmin = identificacionAdmin;
        this.correoAdmin = correoAdmin;
        this.passwordAdmin = passwordAdmin;
    }

    public Administrador(String nombreAdmin, String apellidoAdmin, String identificacionAdmin,
                         String correoAdmin, String passwordAdmin, String telefono, String direccion) {
        this(nombreAdmin, apellidoAdmin, identificacionAdmin, correoAdmin, passwordAdmin);
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreAdmin() {
        return nombreAdmin;
    }

    public void setNombreAdmin(String nombreAdmin) {
        this.nombreAdmin = nombreAdmin;
        this.actualizarFechaModificacion();
    }

    public String getApellidoAdmin() {
        return apellidoAdmin;
    }

    public void setApellidoAdmin(String apellidoAdmin) {
        this.apellidoAdmin = apellidoAdmin;
        this.actualizarFechaModificacion();
    }

    public String getIdentificacionAdmin() {
        return identificacionAdmin;
    }

    public void setIdentificacionAdmin(String identificacionAdmin) {
        this.identificacionAdmin = identificacionAdmin;
        this.actualizarFechaModificacion();
    }

    public String getCorreoAdmin() {
        return correoAdmin;
    }

    public void setCorreoAdmin(String correoAdmin) {
        this.correoAdmin = correoAdmin;
        this.actualizarFechaModificacion();
    }

    public String getPasswordAdmin() {
        return passwordAdmin;
    }

    public void setPasswordAdmin(String passwordAdmin) {
        this.passwordAdmin = passwordAdmin;
        this.actualizarFechaModificacion();
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
        this.actualizarFechaModificacion();
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
        this.actualizarFechaModificacion();
    }

    public LocalDateTime getResetPasswordTokenExpiry() {
        return resetPasswordTokenExpiry;
    }

    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) {
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
        this.actualizarFechaModificacion();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        this.actualizarFechaModificacion();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
        this.actualizarFechaModificacion();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
        this.actualizarFechaModificacion();
    }

    // Métodos de utilidad
    private void actualizarFechaModificacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getNombreCompleto() {
        return nombreAdmin + " " + apellidoAdmin;
    }

    public void desactivar() {
        this.activo = false;
        this.actualizarFechaModificacion();
    }

    public void activar() {
        this.activo = true;
        this.actualizarFechaModificacion();
    }

    public boolean isTokenValido() {
        return resetPasswordToken != null &&
                resetPasswordTokenExpiry != null &&
                LocalDateTime.now().isBefore(resetPasswordTokenExpiry);
    }

    public void limpiarTokenRecuperacion() {
        this.resetPasswordToken = null;
        this.resetPasswordTokenExpiry = null;
        this.actualizarFechaModificacion();
    }

    // Métodos equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Administrador that = (Administrador) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id='" + id + '\'' +
                ", nombreAdmin='" + nombreAdmin + '\'' +
                ", apellidoAdmin='" + apellidoAdmin + '\'' +
                ", identificacionAdmin='" + identificacionAdmin + '\'' +
                ", correoAdmin='" + correoAdmin + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}