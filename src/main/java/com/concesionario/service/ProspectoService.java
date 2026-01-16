package com.concesionario.service;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Prospecto;
import com.concesionario.repository.ProspectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProspectoService {

    @Autowired
    private ProspectoRepository prospectoRepository;

    public List<ProspectoDTO> obtenerProspectosParaAsesor(String asesorId) {
        List<Prospecto> prospectos = prospectoRepository.findByTrabajadorId(asesorId);
        return prospectos.stream()
                .map(this::convertirAProspectoDTO)
                .collect(Collectors.toList());
    }

    public void registrarProspectoManual(String nombre, String apellido, String correo, String telefono, String vehiculoInteres, String asesorId, String observaciones) {
        Prospecto prospecto = new Prospecto();
        prospecto.setNombre(nombre);
        prospecto.setApellido(apellido);
        prospecto.setCorreo(correo);
        prospecto.setTelefono(telefono);
        prospecto.setVehiculoInteres(vehiculoInteres);
        prospecto.setTrabajadorId(asesorId);
        prospecto.setObservaciones(observaciones);
        prospecto.setOrigen("Presencial");
        prospecto.setEstado("Nuevo");
        prospecto.setFechaRegistro(LocalDateTime.now());
        
        prospectoRepository.save(prospecto);
    }

    private ProspectoDTO convertirAProspectoDTO(Prospecto prospecto) {
        ProspectoDTO dto = new ProspectoDTO();
        dto.setId(prospecto.getId());
        dto.setNombreCompleto(prospecto.getNombre() + " " + prospecto.getApellido());
        dto.setEmail(prospecto.getCorreo());
        dto.setTelefono(prospecto.getTelefono());
        dto.setVehiculoInteres(prospecto.getVehiculoInteres());
        dto.setEstado(prospecto.getEstado());
        dto.setUltimoContacto(prospecto.getUltimoContacto() != null ? prospecto.getUltimoContacto() : prospecto.getFechaRegistro());
        dto.setOrigen(prospecto.getOrigen());
        dto.setObservaciones(prospecto.getObservaciones());
        return dto;
    }

    public void cambiarEstadoContactado(String prospectoId) {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new RuntimeException("Prospecto no encontrado"));
        
        // Solo cambiar estado a "Contactado" si está en "Nuevo". 
        // Si ya está en una fase avanzada (En Proceso, Financiación, etc.), NO cambiar el estado.
        if ("Nuevo".equalsIgnoreCase(prospecto.getEstado())) {
            prospecto.setEstado("Contactado");
        }
        
        prospecto.setUltimoContacto(LocalDateTime.now());
        prospectoRepository.save(prospecto);
    }

    public void actualizarEstadoProspecto(String prospectoId, String nuevoEstado) {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new RuntimeException("Prospecto no encontrado"));
        prospecto.setEstado(nuevoEstado);
        prospecto.setUltimoContacto(LocalDateTime.now());
        prospectoRepository.save(prospecto);
    }
}
