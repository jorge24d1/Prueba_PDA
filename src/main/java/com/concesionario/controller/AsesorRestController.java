package com.concesionario.controller;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Cita;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.CitaRepository;
import com.concesionario.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/asesor")
public class AsesorRestController {

    @Autowired
    private TrabajadorDetailsService trabajadorDetailsService;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ProspectoService prospectoService;

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/prospectos")
    public List<ProspectoDTO> obtenerProspectos(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            return prospectoService.obtenerProspectosParaAsesor(asesor.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener prospectos: " + e.getMessage());
        }
    }

    @PostMapping("/prospectos/nuevo")
    public ResponseEntity<?> registrarNuevoProspecto(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String correo,
            @RequestParam String telefono,
            @RequestParam String vehiculoInteres,
            @RequestParam(required = false) String observaciones,
            Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            
            prospectoService.registrarProspectoManual(
                nombre, apellido, correo, telefono, vehiculoInteres, asesor.getId(), observaciones
            );
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Prospecto registrado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/prospectos/contactar")
    public ResponseEntity<?> marcarComoContactado(@RequestParam String prospectoId) {
        try {
            prospectoService.cambiarEstadoContactado(prospectoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @PostMapping("/prospectos/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoProspecto(@RequestParam String prospectoId, @RequestParam String nuevoEstado) {
        try {
            prospectoService.actualizarEstadoProspecto(prospectoId, nuevoEstado);
            return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/datos")
    public ResponseEntity<?> obtenerDatosAsesor(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            // Obtener todos los prospectos asignados al asesor
            List<ProspectoDTO> prospectos = prospectoService.obtenerProspectosParaAsesor(asesor.getId());
            LocalDateTime ahora = LocalDateTime.now();

            // 1. Prospectos Activos: Total de prospectos ATENDIDOS (Estado NO es "Nuevo")
            long prospectosActivos = prospectos.stream()
                    .filter(p -> !"Nuevo".equalsIgnoreCase(p.getEstado()))
                    .count();

            // 2. Ventas este Mes: Estado "Venta Exitosa" y fecha ultimo contacto en mes actual
            long ventasMes = prospectos.stream()
                    .filter(p -> "Venta Exitosa".equalsIgnoreCase(p.getEstado()) && 
                                 p.getUltimoContacto() != null &&
                                 p.getUltimoContacto().getMonth() == ahora.getMonth() &&
                                 p.getUltimoContacto().getYear() == ahora.getYear())
                    .count();

            // 3. Tasa de Conversión: (Ventas Exitosas / Prospectos Atendidos) * 100
            double tasaConversion = 0.0;
            if (prospectosActivos > 0) {
                tasaConversion = ((double) ventasMes / prospectosActivos) * 100.0;
            }

            // 4. Comisiones: 10,000 COP por cada venta exitosa del mes
            double comisiones = ventasMes * 10000.0;

            // Crear respuesta
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("nombre", asesor.getNombre());
            
            // "totalProspectos" se usará para mostrar "Prospectos Activos" en el frontend
            response.put("totalProspectos", prospectosActivos); 
            response.put("ventasMes", ventasMes);
            response.put("tasaConversion", Math.round(tasaConversion * 10.0) / 10.0); // Redondear a 1 decimal
            response.put("comisiones", comisiones);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener datos: " + e.getMessage());
        }
    }

    @GetMapping("/citas")
    public List<Map<String, Object>> obtenerCitasAsesor(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            List<Cita> citas = citaRepository.findByTrabajadorId(asesor.getId());

            return citas.stream().map(cita -> {
                Map<String, Object> citaMap = new HashMap<>();

                // Nombre del cliente
                String nombreCompleto = "";
                if (cita.getUsuario() != null) {
                    nombreCompleto = (cita.getUsuario().getNombre() != null ? cita.getUsuario().getNombre() : "") + " " +
                            (cita.getUsuario().getApellido() != null ? cita.getUsuario().getApellido() : "");
                }
                citaMap.put("id", cita.getId());
                citaMap.put("cliente", nombreCompleto.trim());
                citaMap.put("tipo", cita.getTipo() != null ? cita.getTipo() : "No especificado");

                // Vehículo
                String vehiculo = "No especificado";
                if (cita.getVehiculo() != null && cita.getVehiculo().getModelo() != null) {
                    vehiculo = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
                } else if (cita.getNombreVehiculo() != null && !cita.getNombreVehiculo().isEmpty()) {
                    vehiculo = cita.getNombreVehiculo();
                }
                citaMap.put("vehiculo", vehiculo);

                // Fechas
                citaMap.put("fechaSolicitud", cita.getFechaCreacion() != null ? cita.getFechaCreacion() : "");
                citaMap.put("fechaAsignada", cita.getFechaAsignada() != null ? cita.getFechaAsignada() : "");

                // Otros campos
                citaMap.put("comentario", cita.getComentario() != null ? cita.getComentario() : "Sin comentario");
                citaMap.put("estado", cita.getEstado() != null ? cita.getEstado() : "Pendiente");
                citaMap.put("notasAdmin", cita.getNotasAdmin() != null ? cita.getNotasAdmin() : "Sin notas");

                return citaMap;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener citas: " + e.getMessage());
        }
    }

    @PostMapping("/citas/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoCita(@PathVariable String id, @RequestParam String estado) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            cita.setEstado(estado);
            citaRepository.save(cita);

            // 🔔 NOTIFICACIÓN PUSH
            if (cita.getUsuario() != null) {
               notificationService.enviarNotificacion(
                   cita.getUsuario().getId(), 
                   "Actualización de Cita", 
                   "Tu cita ha cambiado de estado a: " + estado
               );
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/citas/{id}/asignar-fecha")
    public ResponseEntity<?> asignarFechaCita(@PathVariable String id,
                                              @RequestParam String fecha,
                                              @RequestParam String hora) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            // Combinar fecha y hora
            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            cita.setFechaAsignada(fechaHora);
            citaRepository.save(cita);

            // 🔔 NOTIFICACIÓN PUSH
            if (cita.getUsuario() != null) {
               notificationService.enviarNotificacion(
                   cita.getUsuario().getId(), 
                   "Cita Asignada", 
                   "Tu cita ha sido programada para el " + fecha + " a las " + hora
               );
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/citas/{id}/guardar-notas")
    public ResponseEntity<?> guardarNotasCita(@PathVariable String id, @RequestParam String notas) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            cita.setNotasAdmin(notas);
            citaRepository.save(cita);

            // 🔔 NOTIFICACIÓN PUSH
            if (cita.getUsuario() != null) {
               notificationService.enviarNotificacion(
                   cita.getUsuario().getId(), 
                   "Nueva Nota en tu Cita", 
                   "El asesor ha agregado una nota: " + (notas.length() > 50 ? notas.substring(0, 47) + "..." : notas)
               );
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/citas/{id}/datos")
    public ResponseEntity<?> obtenerDatosCita(@PathVariable String id) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            Map<String, Object> citaMap = new HashMap<>();
            citaMap.put("id", cita.getId());
            citaMap.put("fechaAsignada", cita.getFechaAsignada());
            citaMap.put("fechaSolicitud", cita.getFechaCreacion());
            citaMap.put("estado", cita.getEstado());
            citaMap.put("notasAdmin", cita.getNotasAdmin());
            
            // Cliente info
            if (cita.getUsuario() != null) {
                citaMap.put("cliente", cita.getUsuario().getNombre() + " " + cita.getUsuario().getApellido());
            } else {
                citaMap.put("cliente", (cita.getNombres() != null ? cita.getNombres() : "") + " " + (cita.getApellidos() != null ? cita.getApellidos() : ""));
            }

            return ResponseEntity.ok(citaMap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos")
    public List<Map<String, Object>> obtenerVehiculosParaAsesor() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();

            return vehiculos.stream().map(vehiculo -> {
                Map<String, Object> vehiculoMap = new HashMap<>();
                vehiculoMap.put("id", vehiculo.getId());
                vehiculoMap.put("marca", vehiculo.getMarca());
                vehiculoMap.put("modelo", vehiculo.getModelo());
                vehiculoMap.put("año", vehiculo.getAño());
                vehiculoMap.put("precio", vehiculo.getPrecio());
                vehiculoMap.put("categoria", vehiculo.getCategoria());
                vehiculoMap.put("imagenUrl", vehiculo.getImagenUrl());
                vehiculoMap.put("motor", vehiculo.getMotor());
                vehiculoMap.put("transmision", vehiculo.getTransmision());
                vehiculoMap.put("combustible", vehiculo.getCombustible());
                vehiculoMap.put("pasajeros", vehiculo.getPasajeros());
                vehiculoMap.put("descripcion", vehiculo.getDescripcion());
                vehiculoMap.put("colores", vehiculo.getColores() != null ? vehiculo.getColores() : new ArrayList<>());
                vehiculoMap.put("destacado", vehiculo.isDestacado());

                return vehiculoMap;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener vehículos: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos/marcas")
    public List<String> obtenerMarcasDisponibles() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
            return vehiculos.stream()
                    .map(Vehiculo::getMarca)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener marcas: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos/categorias")
    public List<String> obtenerCategoriasDisponibles() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
            return vehiculos.stream()
                    .map(Vehiculo::getCategoria)
                    .filter(categoria -> categoria != null && !categoria.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener categorías: " + e.getMessage());
        }
    }

    @PostMapping("/vehiculos/{id}/compartir")
    public ResponseEntity<?> compartirVehiculoConClientes(@PathVariable String id, Principal principal) {
        try {
            // Verificar que el asesor está autenticado
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            System.out.println("🔄 Iniciando envío masivo por asesor: " + asesor.getNombre());

            // Enviar promoción masiva
            emailService.enviarPromocionVehiculo(id);

            // Registrar la acción
            System.out.println("✅ Promoción masiva completada por asesor: " + asesor.getNombre());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Promoción enviada exitosamente a todos los clientes"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error en envío masivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al enviar la promoción: " + e.getMessage()
            ));
        }
    }
}
