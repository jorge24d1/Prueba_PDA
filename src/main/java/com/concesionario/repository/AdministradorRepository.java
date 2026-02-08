package com.concesionario.repository;

import com.concesionario.model.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends MongoRepository<Administrador, String> {

    // Verificación de existencia
    boolean existsByCorreoAdmin(String correoAdmin);
    boolean existsByIdentificacionAdmin(String identificacionAdmin);

    // Búsquedas por credenciales
    Optional<Administrador> findByCorreoAdmin(String correoAdmin);
    Optional<Administrador> findByIdentificacionAdmin(String identificacionAdmin);

    // ✅ NUEVO: Búsqueda por token de recuperación
    Optional<Administrador> findByResetPasswordToken(String resetPasswordToken);

    // ✅ NUEVO: Buscar administradores activos
    List<Administrador> findByActivoTrue();

    // ✅ NUEVO: Buscar por nombre o apellido (búsqueda flexible)
    List<Administrador> findByNombreAdminContainingIgnoreCaseOrApellidoAdminContainingIgnoreCase(String nombre, String apellido);
}