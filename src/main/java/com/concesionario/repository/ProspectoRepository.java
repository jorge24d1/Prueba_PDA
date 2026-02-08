package com.concesionario.repository;

import com.concesionario.model.Prospecto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProspectoRepository extends MongoRepository<Prospecto, String> {
    

    List<Prospecto> findByTrabajadorId(String trabajadorId);


    Prospecto findByCedula(String cedula);
    Prospecto findByCorreo(String correo);


    List<Prospecto> findByTrabajadorIdAndEstado(String trabajadorId, String estado);

    List<Prospecto> findByTrabajadorIdAndFechaRegistroBetween(
            String trabajadorId, LocalDateTime inicio, LocalDateTime fin);

    List<Prospecto> findByTrabajadorIdAndEstadoAndFechaRegistroBetween(
            String trabajadorId, String estado, LocalDateTime inicio, LocalDateTime fin);


    @Query(value = "{'trabajadorId': ?0, 'estado': ?1, 'fechaRegistro': {$gte: ?2, $lte: ?3}}", count = true)
    long countByTrabajadorIdAndEstadoAndFechaRegistroBetween(
            String trabajadorId, String estado, LocalDateTime inicio, LocalDateTime fin);
}
