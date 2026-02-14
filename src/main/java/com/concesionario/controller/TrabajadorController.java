package com.concesionario.controller;


import com.concesionario.utils.SecurityUtils;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.CitaRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.Principal;

import java.util.*;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;



@Controller
public class TrabajadorController {


    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private TrabajadorDetailsService trabajadorDetailsService;



    @Autowired
    private ProspectoService prospectoService;

    @Autowired
    private PrediccionService prediccionService;

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/perfil_analisis")
    public String perfilA(Model model, Authentication authentication) {
        long totalClientes = usuarioRepository.count();
        long totalVehiculos = vehiculoRepository.count();
        long totalCitas = citaRepository.count();
        long totalTrabajadores = trabajadorRepository.count();

        String nombreUsuario = "Analista";

        if (authentication != null) {
            String username = SecurityUtils.getEmailFromPrincipal(authentication);
            Optional<Trabajador> trabajador = trabajadorRepository.findByCorreo(username);
            if (trabajador.isPresent()) {
                nombreUsuario = trabajador.get().getNombre();
            }
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalVehiculos", totalVehiculos);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalTrabajadores", totalTrabajadores);

        return "perfil_analisis";
    }

    @GetMapping("/perfil_gestor")
    public String perfilG(Model model, Authentication authentication) {
        // Obtener datos para el dashboard del gestor
        List<Vehiculo> vehiculos = vehiculoService.obtenerVehiculosNormales();
        List<Vehiculo> anuncios = vehiculoService.obtenerDestacados();
        long totalVehiculos = vehiculoRepository.count();
        long totalAnuncios = anuncios.size();

        // Obtener nombre del gestor autenticado
        String nombreUsuario = "Gestor"; // Valor por defecto

        if (authentication != null) {
            String username = SecurityUtils.getEmailFromPrincipal(authentication);
            Optional<Trabajador> trabajador = trabajadorRepository.findByCorreo(username);
            if (trabajador.isPresent()) {
                nombreUsuario = trabajador.get().getNombre();
            }
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("anuncios", anuncios);
        model.addAttribute("totalVehiculos", totalVehiculos);
        model.addAttribute("totalAnuncios", totalAnuncios);

        return "perfil_gestor";
    }

    @GetMapping("/perfil_asesor")
    public String perfilAsesor(Model model, Principal principal) {
        String nombreUsuario = "Asesor";
        if (principal != null) {
            String email = SecurityUtils.getEmailFromPrincipal(principal);
            Trabajador trabajador = trabajadorDetailsService.findByCorreo(email);
            if (trabajador != null) {
                nombreUsuario = trabajador.getNombre();
                // Opcional: Agregar ID si se necesita en la vista inicial para algo
                model.addAttribute("asesorId", trabajador.getId());
            }
        }
        model.addAttribute("nombreUsuario", nombreUsuario);
        return "perfil_asesor";
    }

    @GetMapping("/gestor/descargar-reporte-potenciales")
    public ResponseEntity<InputStreamResource> descargarReportePotenciales() {
        try {
            // Obtener y procesar usuarios
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Usuario> usuariosProcesados = usuarios.stream()
                    .map(this::aplicarPrediccionYActualizar)
                    .filter(u -> "Si".equals(u.getClientePotencial()))
                    .toList();

            ByteArrayInputStream in = reporteService.generarReportePotenciales(usuariosProcesados);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Disposition", "attachment; filename=Reporte_Usuarios_Potenciales_NextGen.xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte Excel", e);
        }
    }

    // Método para aplicar predicción a cada usuario
    private Usuario aplicarPrediccionYActualizar(Usuario usuario) {
        try {
            // Obtener valores para la predicción (usar valores por defecto si son null)
            double citas = usuario.getCantidadCitas() != null ? usuario.getCantidadCitas() : 0;
            double antiguedad = usuario.getAntiguedadCuenta() != null ? usuario.getAntiguedadCuenta() : 0;
            String estado = usuario.getEstadoUltimaCita() != null ? usuario.getEstadoUltimaCita() : "Pendiente";
            String interes = usuario.getInteresVehiculo() != null ? usuario.getInteresVehiculo() : "No";
            double tiempo = usuario.getTiempoEntreCitas() != null ? usuario.getTiempoEntreCitas() : 0;

            // Usar tu servicio de predicción
            String prediccion = prediccionService.predecir(citas, antiguedad, estado, interes, tiempo);
            double probabilidad = prediccionService.obtenerProbabilidadSi(citas, antiguedad, estado, interes, tiempo);

            // Actualizar usuario con la predicción
            usuario.setClientePotencial(prediccion);
            usuario.setProbabilidad(probabilidad);
            usuario.setObservaciones(generarObservaciones(prediccion, probabilidad, citas, estado));

            return usuario;

        } catch (Exception e) {
            System.err.println("Error aplicando predicción para usuario " + usuario.getCorreoUser() + ": " + e.getMessage());
            // En caso de error, marcar como no potencial
            usuario.setClientePotencial("No");
            usuario.setProbabilidad(0.0);
            usuario.setObservaciones("Error en análisis predictivo");
            return usuario;
        }
    }



    private String generarObservaciones(String prediccion, double probabilidad, double citas, String estado) {
        StringBuilder observaciones = new StringBuilder();

        if ("Si".equals(prediccion)) {
            observaciones.append("Cliente potencial identificado. ");
        } else {
            observaciones.append("Requiere seguimiento adicional. ");
        }

        observaciones.append("Probabilidad: ").append(String.format("%.1f", probabilidad)).append("%. ");

        if (citas == 0) {
            observaciones.append("Sin citas previas. ");
        } else if (citas >= 3) {
            observaciones.append("Alto nivel de interés demostrado. ");
        }

        if ("Completada".equals(estado)) {
            observaciones.append("Última cita completada exitosamente.");
        } else if ("Cancelada".equals(estado)) {
            observaciones.append("Última cita cancelada.");
        }

        return observaciones.toString();
    }
    @GetMapping("/gestor/descargar-analisis-rendimiento")
    public ResponseEntity<InputStreamResource> descargarAnalisisRendimiento() {
        try {
            ByteArrayInputStream in = prospectoService.generarReporteRendimientoMensual();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Analisis_Rendimiento_Asesores.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}