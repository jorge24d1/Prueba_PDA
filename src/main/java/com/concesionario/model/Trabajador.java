package com.concesionario.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trabajadores")
public class Trabajador {
    @Id
    private String id;

    @Indexed(unique = true)
    private String identificacion;

    private String nombre;
    private String apellido;

    @Indexed(unique = true)
    private String correo;

    private String password;
    private List<Rol> roles = new ArrayList<>();

    // Horario laboral
    private LocalTime horaInicioTrabajo;
    private LocalTime horaFinTrabajo;
    private List<String> diasTrabajo; // ["LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"]

    // Campos para recuperación de contraseña
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;

    // Campos de auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean activo = true;

    // Información de contacto
    private String telefono;
    private String direccion;
    private String cargo;
    private Double salario;

    // Información adicional
    private String departamento;
    private LocalDateTime fechaContratacion;
    private String tipoContrato; // "TIEMPO_COMPLETO", "MEDIO_TIEMPO", "TEMPORAL"
    private String observaciones;

    // Constructores
    public Trabajador() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.roles = new ArrayList<>();
        this.diasTrabajo = new ArrayList<>();
    }

    public Trabajador(String nombre, String apellido, String identificacion, String correo, String password) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.correo = correo;
        this.password = password;
    }

    public Trabajador(String nombre, String apellido, String identificacion, String correo,
                      String password, String telefono, String direccion, String cargo) {
        this(nombre, apellido, identificacion, correo, password);
        this.telefono = telefono;
        this.direccion = direccion;
        this.cargo = cargo;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
        this.actualizarFechaModificacion();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.actualizarFechaModificacion();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        this.actualizarFechaModificacion();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
        this.actualizarFechaModificacion();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.actualizarFechaModificacion();
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
        this.actualizarFechaModificacion();
    }

    public LocalTime getHoraInicioTrabajo() {
        return horaInicioTrabajo;
    }

    public void setHoraInicioTrabajo(LocalTime horaInicioTrabajo) {
        this.horaInicioTrabajo = horaInicioTrabajo;
        this.actualizarFechaModificacion();
    }

    public LocalTime getHoraFinTrabajo() {
        return horaFinTrabajo;
    }

    public void setHoraFinTrabajo(LocalTime horaFinTrabajo) {
        this.horaFinTrabajo = horaFinTrabajo;
        this.actualizarFechaModificacion();
    }

    public List<String> getDiasTrabajo() {
        return diasTrabajo;
    }

    public void setDiasTrabajo(List<String> diasTrabajo) {
        this.diasTrabajo = diasTrabajo;
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

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
        this.actualizarFechaModificacion();
    }

    public Double getSalario() {
        return salario;
    }

    public void setSalario(Double salario) {
        this.salario = salario;
        this.actualizarFechaModificacion();
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
        this.actualizarFechaModificacion();
    }

    public LocalDateTime getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDateTime fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
        this.actualizarFechaModificacion();
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
        this.actualizarFechaModificacion();
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        this.actualizarFechaModificacion();
    }

    // Métodos de utilidad
    private void actualizarFechaModificacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // Métodos para manejar roles
    public void agregarRol(Rol rol) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        if (!this.roles.contains(rol)) {
            this.roles.add(rol);
            this.actualizarFechaModificacion();
        }
    }

    public void removerRol(Rol rol) {
        if (this.roles != null) {
            this.roles.remove(rol);
            this.actualizarFechaModificacion();
        }
    }

    public boolean tieneRol(Rol rol) {
        return this.roles != null && this.roles.contains(rol);
    }

    public boolean tieneAlgunRol(Rol... roles) {
        if (this.roles == null) return false;
        for (Rol rol : roles) {
            if (this.roles.contains(rol)) {
                return true;
            }
        }
        return false;
    }

    public void limpiarRoles() {
        if (this.roles != null) {
            this.roles.clear();
            this.actualizarFechaModificacion();
        }
    }

    public List<String> getNombresRoles() {
        List<String> nombres = new ArrayList<>();
        if (this.roles != null) {
            for (Rol rol : this.roles) {
                nombres.add(rol.name());
            }
        }
        return nombres;
    }

    // Métodos para manejar días de trabajo
    public void agregarDiaTrabajo(String dia) {
        if (this.diasTrabajo == null) {
            this.diasTrabajo = new ArrayList<>();
        }
        if (!this.diasTrabajo.contains(dia.toUpperCase())) {
            this.diasTrabajo.add(dia.toUpperCase());
            this.actualizarFechaModificacion();
        }
    }

    public void removerDiaTrabajo(String dia) {
        if (this.diasTrabajo != null) {
            this.diasTrabajo.remove(dia.toUpperCase());
            this.actualizarFechaModificacion();
        }
    }

    public boolean trabajaElDia(String dia) {
        return this.diasTrabajo != null && this.diasTrabajo.contains(dia.toUpperCase());
    }

    // Métodos de estado
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

    // Método para verificar si está trabajando en un momento dado
    public boolean estaTrabajandoAhora() {
        if (horaInicioTrabajo == null || horaFinTrabajo == null) {
            return false;
        }

        LocalTime ahora = LocalTime.now();
        return !ahora.isBefore(horaInicioTrabajo) && !ahora.isAfter(horaFinTrabajo);
    }

    // Métodos equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trabajador that = (Trabajador) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Trabajador{" +
                "id='" + id + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", roles=" + roles +
                ", activo=" + activo +
                ", cargo='" + cargo + '\'' +
                ", departamento='" + departamento + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}