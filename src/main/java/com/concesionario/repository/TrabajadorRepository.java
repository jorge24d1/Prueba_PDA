package com.concesionario.repository;

import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TrabajadorRepository extends MongoRepository<Trabajador, String> {

    // Búsquedas por credenciales
    Optional<Trabajador> findByCorreo(String correo);
    Optional<Trabajador> findByIdentificacion(String identificacion);

    // Verificación de existencia
    boolean existsByCorreo(String correo);
    boolean existsByIdentificacion(String identificacion);

    // ✅ NUEVO: Búsqueda por token de recuperación
    Optional<Trabajador> findByResetPasswordToken(String resetPasswordToken);

    // Búsquedas por roles
    List<Trabajador> findByRolesContaining(Rol rol);

    // ✅ NUEVO: Buscar trabajadores activos
    List<Trabajador> findByActivoTrue();

    // ✅ NUEVO: Buscar por departamento
    List<Trabajador> findByDepartamento(String departamento);

    // ✅ NUEVO: Buscar por cargo
    List<Trabajador> findByCargo(String cargo);

    // ✅ NUEVO: Buscar por tipo de contrato
    List<Trabajador> findByTipoContrato(String tipoContrato);

    // ✅ NUEVO: Buscar por nombre o apellido (búsqueda flexible)
    List<Trabajador> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // ✅ NUEVO: Búsqueda avanzada con múltiples criterios
    @Query("{'activo': true, 'departamento': ?0, 'cargo': ?1}")
    List<Trabajador> findActivosByDepartamentoAndCargo(String departamento, String cargo);

    // ✅ NUEVO: Buscar trabajadores con salario mayor o igual a
    List<Trabajador> findBySalarioGreaterThanEqual(Double salario);

    // ✅ NUEVO: Buscar trabajadores con salario entre rango
    List<Trabajador> findBySalarioBetween(Double salarioMin, Double salarioMax);

    // ✅ NUEVO: Buscar por múltiples roles
    @Query("{'roles': { $in: ?0 }}")
    List<Trabajador> findByRolesIn(List<Rol> roles);

    // ✅ NUEVO: Contar trabajadores por departamento
    @Query(value = "{'departamento': ?0}", count = true)
    long countByDepartamento(String departamento);

    // ✅ NUEVO: Contar trabajadores activos por rol
    @Query(value = "{'activo': true, 'roles': ?0}", count = true)
    long countActivosByRol(Rol rol);
}