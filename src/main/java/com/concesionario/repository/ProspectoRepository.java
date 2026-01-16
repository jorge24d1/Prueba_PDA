package com.concesionario.repository;

import com.concesionario.model.Prospecto;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProspectoRepository extends MongoRepository<Prospecto, String> {
    
    // Buscar prospectos asignados a un trabajador específico
    List<Prospecto> findByTrabajadorId(String trabajadorId);

    // Buscar por cédula o correo para evitar duplicados
    Prospecto findByCedula(String cedula);
    Prospecto findByCorreo(String correo);
}
