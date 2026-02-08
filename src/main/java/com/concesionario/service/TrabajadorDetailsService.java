package com.concesionario.service;

import com.concesionario.model.Trabajador;
import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.CitaRepository;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class TrabajadorDetailsService implements UserDetailsService {

    private final TrabajadorRepository trabajadorRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public TrabajadorDetailsService(TrabajadorRepository trabajadorRepository,
                                    CitaRepository citaRepository,
                                    UsuarioRepository usuarioRepository) {
        this.trabajadorRepository = trabajadorRepository;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Trabajador findByCorreo(String correo) {
        return trabajadorRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Trabajador no encontrado"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Trabajador trabajador = trabajadorRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Trabajador no encontrado"));

        String[] rolesArray = trabajador.getRoles().stream()
                .map(rol -> rol.name())
                .toArray(String[]::new);

        return User.builder()
                .username(trabajador.getCorreo())
                .password(trabajador.getPassword())
                .roles(rolesArray)
                .build();
    }

    // ========== MÉTODOS CORREGIDOS PARA PROSPECTOS ==========

    /**
     * Obtiene los datos del asesor para el dashboard
     */
    public Map<String, Object> obtenerDatosAsesor(String asesorId) {
        Trabajador asesor = trabajadorRepository.findById(asesorId)
                .orElseThrow(() -> new UsernameNotFoundException("Asesor no encontrado"));

        // Obtener citas del asesor para calcular métricas
        List<Cita> citasAsesor = citaRepository.findByTrabajadorId(asesorId);

        // ✅ CORREGIR: Filtrar citas con usuario no nulo
        List<Cita> citasValidas = citasAsesor.stream()
                .filter(cita -> cita.getUsuario() != null)
                .collect(Collectors.toList());

        // Calcular métricas
        long totalProspectos = citasValidas.stream()
                .map(cita -> cita.getUsuario().getId())
                .distinct()
                .count();

        long ventasMes = citasValidas.stream()
                .filter(cita -> "Aprobada".equals(cita.getEstado()))
                .count();

        double tasaConversion = citasValidas.isEmpty() ? 0 :
                (ventasMes * 100.0) / citasValidas.size();

        double comisiones = ventasMes * 850.0;

        return Map.of(
                "nombre", asesor.getNombre(),
                "totalProspectos", totalProspectos,
                "ventasMes", ventasMes,
                "tasaConversion", Math.round(tasaConversion),
                "comisiones", comisiones
        );
    }

    /**
     * Obtiene los prospectos asignados a un asesor
     */
    public List<Map<String, Object>> obtenerProspectosParaAsesor(String asesorId) {
        List<Cita> citasAsesor = citaRepository.findByTrabajadorId(asesorId);

        // ✅ CORREGIR: Filtrar citas con usuario no nulo
        List<Cita> citasValidas = citasAsesor.stream()
                .filter(cita -> cita.getUsuario() != null)
                .collect(Collectors.toList());

        // Agrupar por usuario y obtener la última cita de cada uno
        Map<String, Cita> ultimasCitasPorUsuario = citasValidas.stream()
                .collect(Collectors.toMap(
                        cita -> cita.getUsuario().getId(),
                        cita -> cita,
                        (cita1, cita2) -> cita1.getFechaCreacion().isAfter(cita2.getFechaCreacion()) ? cita1 : cita2
                ));

        // Convertir a formato para el frontend
        return ultimasCitasPorUsuario.values().stream()
                .map(this::convertirCitaAProspecto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertirCitaAProspecto(Cita cita) {
        Map<String, Object> prospecto = new HashMap<>();

        // ✅ CORREGIR: Obtener nombres y apellidos del USUARIO EMBEBIDO, no de la cita directamente
        String nombreCompleto = "";
        if (cita.getUsuario() != null) {
            // Los datos reales están en el usuario embebido
            nombreCompleto = (cita.getUsuario().getNombre() != null ? cita.getUsuario().getNombre() : "") + " " +
                    (cita.getUsuario().getApellido() != null ? cita.getUsuario().getApellido() : "");
        } else {
            // Fallback a los campos de la cita (aunque sean null en tu caso)
            nombreCompleto = (cita.getNombres() != null ? cita.getNombres() : "") + " " +
                    (cita.getApellidos() != null ? cita.getApellidos() : "");
        }

        String vehiculoInteres = "No especificado";
        if (cita.getVehiculo() != null && cita.getVehiculo().getModelo() != null) {
            // Si existe el vehículo embebido
            vehiculoInteres = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
        } else if (cita.getNombreVehiculo() != null && !cita.getNombreVehiculo().isEmpty()) {
            // Si existe nombreVehiculo
            vehiculoInteres = cita.getNombreVehiculo();
        }

        // ✅ CORREGIR: También obtener email del usuario embebido
        String email = "";
        if (cita.getUsuario() != null && cita.getUsuario().getCorreo() != null) {
            email = cita.getUsuario().getCorreo();
        } else {
            email = cita.getCorreoElectronico() != null ? cita.getCorreoElectronico() : "";
        }

        prospecto.put("usuarioId", cita.getUsuario() != null ? cita.getUsuario().getId() : "");
        prospecto.put("nombreCompleto", nombreCompleto.trim());
        prospecto.put("email", email);
        prospecto.put("telefono", cita.getTelefono() != null ? cita.getTelefono() : "Sin teléfono");
        prospecto.put("vehiculoInteres", vehiculoInteres);
        prospecto.put("estado", cita.getEstado() != null ? cita.getEstado() : "Pendiente");
        prospecto.put("ultimoContacto", cita.getFechaCreacion() != null ? cita.getFechaCreacion() : null);
        prospecto.put("proximaAccion", cita.getFechaAsignada() != null ? cita.getFechaAsignada() : null);
        prospecto.put("citaId", cita.getId() != null ? cita.getId() : "");

        return prospecto;
    }

    /**
     * Cambia el estado de una cita a "Contactado"
     */
    public void marcarComoContactado(String citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado("Contactado");
        citaRepository.save(cita);
    }

    public List<Map<String, Object>> obtenerCitasProximas(String asesorId) {
        List<Cita> citas = citaRepository.findByTrabajadorIdAndFechaAsignadaIsNotNull(asesorId);

        return citas.stream()
                .filter(cita -> cita.getFechaAsignada() != null)
                .map(cita -> {
                    // ✅ CORREGIR: Obtener nombre del usuario embebido
                    String nombreCompleto = "";
                    if (cita.getUsuario() != null) {
                        nombreCompleto = (cita.getUsuario().getNombre() != null ? cita.getUsuario().getNombre() : "") + " " +
                                (cita.getUsuario().getApellido() != null ? cita.getUsuario().getApellido() : "");
                    } else {
                        nombreCompleto = (cita.getNombres() != null ? cita.getNombres() : "") + " " +
                                (cita.getApellidos() != null ? cita.getApellidos() : "");
                    }

                    String vehiculo = "No especificado";
                    if (cita.getVehiculo() != null && cita.getVehiculo().getModelo() != null) {
                        vehiculo = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
                    } else if (cita.getNombreVehiculo() != null && !cita.getNombreVehiculo().isEmpty()) {
                        vehiculo = cita.getNombreVehiculo();
                    }

                    Map<String, Object> citaMap = new HashMap<>();
                    citaMap.put("cliente", nombreCompleto.trim());
                    citaMap.put("vehiculo", vehiculo);
                    citaMap.put("tipo", obtenerTipoCita(cita));
                    citaMap.put("fecha", cita.getFechaAsignada().toLocalDate().toString());
                    citaMap.put("hora", cita.getFechaAsignada().toLocalTime().toString());

                    return citaMap;
                })
                .collect(Collectors.toList());
    }

    private String obtenerTipoCita(Cita cita) {
        if (cita.getTipo() != null) {
            return cita.getTipo();
        }
        return "Test Drive";
    }
}