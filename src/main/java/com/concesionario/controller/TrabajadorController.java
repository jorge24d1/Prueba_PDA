package com.concesionario.controller;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Cita;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.CitaRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.service.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Importaciones para Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Arrays;


@Controller
public class TrabajadorController {

    @Autowired
    private EmailPromocionalService emailPromocionalService;
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
    private TrabajadorDetailsService trabajadorDetailsService; // ✅ AÑADIR ESTO

    @Autowired
    private ProspectoService prospectoService;

    @Autowired
    private PrediccionService prediccionService;

    @GetMapping("/perfil_analisis")
    public String perfilA(Model model, Authentication authentication) {
        long totalClientes = usuarioRepository.count();
        long totalVehiculos = vehiculoRepository.count();
        long totalCitas = citaRepository.count();
        long totalTrabajadores = trabajadorRepository.count();

        String nombreUsuario = "Analista";

        if (authentication != null) {
            String username = authentication.getName();
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

        return "Perfil_analisis";
    }

    @GetMapping("/perfil_gestor")
    public String perfilG(Model model) {
        // Obtener datos para el dashboard del gestor
        List<Vehiculo> vehiculos = vehiculoService.obtenerVehiculosNormales();
        List<Vehiculo> anuncios = vehiculoService.obtenerDestacados();
        long totalVehiculos = vehiculoRepository.count();
        long totalAnuncios = anuncios.size();



        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("anuncios", anuncios);
        model.addAttribute("totalVehiculos", totalVehiculos);
        model.addAttribute("totalAnuncios", totalAnuncios);


        return "Perfil_gestor";
    }

    @GetMapping("/perfil_asesor")
    public String perfilAsesor(Model model, Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            model.addAttribute("asesorId", asesor.getId());
            model.addAttribute("nombreAsesor", asesor.getNombre());

            // ✅ Usar ProspectoService que ya tienes
            long totalProspectos = prospectoService.obtenerProspectosParaAsesor(asesor.getId()).size();
            model.addAttribute("totalProspectos", totalProspectos);

            return "perfil_asesor";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // ✅ MOVER ESTOS ENDPOINTS A UN CONTROLADOR API SEPARADO O MANTENERLOS AQUÍ PERO CON RUTAS CORRECTAS
    @GetMapping("/asesor/prospectos")
    @ResponseBody
    public List<Map<String, Object>> obtenerProspectos(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            // ✅ DEBUG MEJORADO: Ver datos del usuario embebido
            List<Cita> citas = citaRepository.findByTrabajadorId(asesor.getId());
            System.out.println("=== DEBUG PROSPECTOS MEJORADO ===");
            System.out.println("Total citas: " + citas.size());
            citas.forEach(cita -> {
                System.out.println("Cita ID: " + cita.getId());
                System.out.println("Usuario embebido: " + (cita.getUsuario() != null ? "Sí" : "No"));
                if (cita.getUsuario() != null) {
                    System.out.println("Nombre usuario: '" + cita.getUsuario().getNombre() + "'");
                    System.out.println("Apellido usuario: '" + cita.getUsuario().getApellido() + "'");
                    System.out.println("Email usuario: '" + cita.getUsuario().getCorreo() + "'");
                }
                System.out.println("Nombres cita: '" + cita.getNombres() + "'");
                System.out.println("Apellidos cita: '" + cita.getApellidos() + "'");
                System.out.println("Teléfono: '" + cita.getTelefono() + "'");
                System.out.println("Vehículo embebido: " + (cita.getVehiculo() != null ?
                        cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo() : "null"));
                System.out.println("---");
            });

            return trabajadorDetailsService.obtenerProspectosParaAsesor(asesor.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener prospectos: " + e.getMessage());
        }
    }

    @PostMapping("/asesor/prospectos/contactar")
    @ResponseBody
    public ResponseEntity<?> marcarComoContactado(@RequestParam String citaId) {
        try {
            prospectoService.cambiarEstadoContactado(citaId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    // ✅ AÑADIR ENDPOINT PARA DATOS DEL ASESOR (DASHBOARD)
    @GetMapping("/asesor/datos")
    @ResponseBody
    public ResponseEntity<?> obtenerDatosAsesor(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            // Obtener prospectos para calcular métricas
            List<ProspectoDTO> prospectos = prospectoService.obtenerProspectosParaAsesor(asesor.getId());

            // Calcular métricas
            long totalProspectos = prospectos.size();
            long ventasMes = prospectos.stream()
                    .filter(p -> "Aprobada".equals(p.getEstado()))
                    .count();
            double tasaConversion = totalProspectos > 0 ?
                    (ventasMes * 100.0) / totalProspectos : 0;
            double comisiones = ventasMes * 850.0; // Ejemplo: $850 por venta

            // Crear respuesta
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("nombre", asesor.getNombre());
            response.put("totalProspectos", totalProspectos);
            response.put("ventasMes", ventasMes);
            response.put("tasaConversion", Math.round(tasaConversion));
            response.put("comisiones", comisiones);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener datos: " + e.getMessage());
        }
    }

    // ✅ AÑADIR ENDPOINT PARA CITAS DEL ASESOR
    @GetMapping("/asesor/citas")
    @ResponseBody
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

    @PostMapping("/asesor/citas/{id}/cambiar-estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoCita(@PathVariable String id, @RequestParam String estado) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            cita.setEstado(estado);
            citaRepository.save(cita);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/asesor/citas/{id}/asignar-fecha")
    @ResponseBody
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

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/asesor/citas/{id}/guardar-notas")
    @ResponseBody
    public ResponseEntity<?> guardarNotasCita(@PathVariable String id, @RequestParam String notas) {
        try {
            Cita cita = citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            cita.setNotasAdmin(notas);
            citaRepository.save(cita);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    // ==================== ENDPOINTS PARA VEHÍCULOS DEL ASESOR ====================

    @GetMapping("/asesor/vehiculos")
    @ResponseBody
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

    @GetMapping("/asesor/vehiculos/marcas")
    @ResponseBody
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

    @GetMapping("/asesor/vehiculos/categorias")
    @ResponseBody
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
    // En tu TrabajadorController, añade este método:
    @PostMapping("/asesor/vehiculos/{id}/compartir")
    @ResponseBody
    public ResponseEntity<?> compartirVehiculoConClientes(@PathVariable String id, Principal principal) {
        try {
            // Verificar que el asesor está autenticado
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            System.out.println("🔄 Iniciando envío masivo por asesor: " + asesor.getNombre());

            // Enviar promoción masiva
            emailPromocionalService.enviarPromocionVehiculo(id);

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



    // ==================== MÉTODOS PARA VEHÍCULOS ====================

    @PostMapping("/gestor/guardar-vehiculo")
    public String guardarVehiculoNormal(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam String colores,
            @RequestParam MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAño(año);
        vehiculo.setPrecio(precio);
        vehiculo.setCategoria(categoria);
        vehiculo.setMotor(motor);
        vehiculo.setTransmision(transmision);
        vehiculo.setCombustible(combustible);
        vehiculo.setPasajeros(pasajeros);
        vehiculo.setDescripcion(descripcion);
        vehiculo.setDestacado(false);

        // Procesar los colores
        if (colores != null && !colores.isEmpty()) {
            List<String> listaColores = Arrays.stream(colores.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            vehiculo.setColores(listaColores);
        }

        vehiculoService.crearVehiculoNormal(vehiculo, imagen);
        redirectAttributes.addFlashAttribute("success", "Vehículo guardado exitosamente");
        return "redirect:/perfil_gestor";
    }

    @GetMapping("/gestor/obtener-vehiculo/{id}")
    @ResponseBody
    public Vehiculo obtenerVehiculoParaEdicion(@PathVariable String id) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        if (vehiculo.getColores() == null) {
            vehiculo.setColores(new ArrayList<>());
        }
        return vehiculo;
    }

    @PostMapping("/gestor/editar-vehiculo/{id}")
    public String editarVehiculo(
            @PathVariable String id,
            @ModelAttribute Vehiculo vehiculo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam Integer pasajeros,
            @RequestParam String colores,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        try {
            Vehiculo vehiculoExistente = vehiculoService.obtenerPorId(id);

            // Actualizar imagen si se proporciona
            if (imagen != null && !imagen.isEmpty()) {
                vehiculoService.actualizarImagenVehiculo(vehiculoExistente, imagen);
            }

            // Actualizar campos básicos
            vehiculoExistente.setMarca(vehiculo.getMarca());
            vehiculoExistente.setModelo(vehiculo.getModelo());
            vehiculoExistente.setAño(vehiculo.getAño());
            vehiculoExistente.setPrecio(vehiculo.getPrecio());
            vehiculoExistente.setCategoria(vehiculo.getCategoria());
            vehiculoExistente.setMotor(motor);
            vehiculoExistente.setTransmision(transmision);
            vehiculoExistente.setCombustible(combustible);
            vehiculoExistente.setPasajeros(pasajeros);
            vehiculoExistente.setDescripcion(descripcion);

            // Procesar colores
            if (colores != null && !colores.isEmpty()) {
                List<String> listaColores = Arrays.stream(colores.split(","))
                        .map(String::trim)
                        .filter(color -> !color.isEmpty())
                        .collect(Collectors.toList());
                vehiculoExistente.setColores(listaColores);
            } else {
                vehiculoExistente.setColores(new ArrayList<>());
            }

            vehiculoService.guardarVehiculo(vehiculoExistente);
            redirectAttributes.addFlashAttribute("success", "Vehículo actualizado exitosamente");
            return "redirect:/perfil_gestor";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar el vehículo: " + e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/gestor/eliminar-vehiculo/{id}")
    public String eliminarVehiculo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo eliminado exitosamente");
        return "redirect:/perfil_gestor";
    }

    // ==================== MÉTODOS PARA ANUNCIOS ====================

    @PostMapping("/gestor/guardar-anuncio")
    public String guardarAnuncio(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam String colores,
            @RequestParam MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

        try {
            Vehiculo anuncio = new Vehiculo();
            anuncio.setMarca(marca);
            anuncio.setModelo(modelo);
            anuncio.setAño(año);
            anuncio.setPrecio(precio);
            anuncio.setCategoria(categoria);
            anuncio.setMotor(motor);
            anuncio.setTransmision(transmision);
            anuncio.setCombustible(combustible);
            anuncio.setPasajeros(pasajeros);
            anuncio.setDescripcion(descripcion);
            anuncio.setDestacado(true); // Es un anuncio

            // Procesar colores
            if (colores != null && !colores.isEmpty()) {
                List<String> listaColores = Arrays.stream(colores.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                anuncio.setColores(listaColores);
            }

            vehiculoService.crearAnuncio(anuncio, imagen);
            redirectAttributes.addFlashAttribute("success", "Anuncio creado exitosamente");
            return "redirect:/perfil_gestor";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el anuncio: " + e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/gestor/eliminar-anuncio/{id}")
    public String eliminarAnuncio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Anuncio eliminado exitosamente");
        return "redirect:/perfil_gestor";
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

            Workbook workbook = new XSSFWorkbook();

            // Crear hoja principal
            Sheet sheet = workbook.createSheet("Usuarios Potenciales");

            // ========== ESTILOS PROFESIONALES ==========

            // Estilo para título principal
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para subtítulo
            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 12);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo para encabezados de tabla
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para celdas de datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para probabilidad alta
            CellStyle highProbStyle = workbook.createCellStyle();
            highProbStyle.cloneStyleFrom(dataStyle);
            highProbStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            highProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            highProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para probabilidad media
            CellStyle mediumProbStyle = workbook.createCellStyle();
            mediumProbStyle.cloneStyleFrom(dataStyle);
            mediumProbStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            mediumProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            mediumProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para probabilidad baja
            CellStyle lowProbStyle = workbook.createCellStyle();
            lowProbStyle.cloneStyleFrom(dataStyle);
            lowProbStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            lowProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            lowProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para resumen
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setBorderTop(BorderStyle.MEDIUM);
            summaryStyle.setBorderBottom(BorderStyle.MEDIUM);

            // ========== CABECERA DEL REPORTE ==========

            // Título principal
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE USUARIOS POTENCIALES - NEXTGEN MOTORS");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

            // Subtítulo
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Análisis Predictivo con Inteligencia Artificial");
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 13));

            // Fecha de generación
            Row dateRow = sheet.createRow(2);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generado: " + java.time.LocalDate.now().toString());
            dateCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 13));

            // Espacio
            sheet.createRow(3);

            // ========== TABLA DE DATOS ==========

            // Encabezados de tabla
            Row headerRow = sheet.createRow(4);
            String[] headers = {
                    "No.", "Nombre", "Apellido", "Identificación", "Correo Electrónico",
                    "Citas", "Antigüedad", "Estado", "Interés", "Tiempo",
                    "Potencial", "Probabilidad", "Confianza", "Observaciones"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 5;
            int contador = 1;
            for (Usuario usuario : usuariosProcesados) {
                Row dataRow = sheet.createRow(rowNum++);

                // Número consecutivo
                dataRow.createCell(0).setCellValue(contador++);

                // Información básica
                dataRow.createCell(1).setCellValue(usuario.getNombreUser() != null ? usuario.getNombreUser() : "N/A");
                dataRow.createCell(2).setCellValue(usuario.getApellidoUser() != null ? usuario.getApellidoUser() : "N/A");
                dataRow.createCell(3).setCellValue(usuario.getIdentificacionUser() != null ? usuario.getIdentificacionUser() : "N/A");
                dataRow.createCell(4).setCellValue(usuario.getCorreoUser() != null ? usuario.getCorreoUser() : "N/A");

                // Métricas
                dataRow.createCell(5).setCellValue(usuario.getCantidadCitas() != null ? usuario.getCantidadCitas() : 0);
                dataRow.createCell(6).setCellValue(usuario.getAntiguedadCuenta() != null ? usuario.getAntiguedadCuenta() : 0);
                dataRow.createCell(7).setCellValue(usuario.getEstadoUltimaCita() != null ? usuario.getEstadoUltimaCita() : "N/A");
                dataRow.createCell(8).setCellValue(usuario.getInteresVehiculo() != null ? usuario.getInteresVehiculo() : "N/A");
                dataRow.createCell(9).setCellValue(usuario.getTiempoEntreCitas() != null ? usuario.getTiempoEntreCitas() : 0);

                // Predicciones
                dataRow.createCell(10).setCellValue(usuario.getClientePotencial() != null ? usuario.getClientePotencial() : "No");

                // Celda de probabilidad con estilo condicional
                Cell probCell = dataRow.createCell(11);
                double probabilidad = usuario.getProbabilidad() != null ? usuario.getProbabilidad() : 0.0;
                probCell.setCellValue(probabilidad);

                if (probabilidad >= 80) {
                    probCell.setCellStyle(highProbStyle);
                } else if (probabilidad >= 60) {
                    probCell.setCellStyle(mediumProbStyle);
                } else {
                    probCell.setCellStyle(lowProbStyle);
                }

                // Nivel de confianza
                dataRow.createCell(12).setCellValue(determinarNivelConfianza(probabilidad));

                // Observaciones
                dataRow.createCell(13).setCellValue(usuario.getObservaciones() != null ? usuario.getObservaciones() : "Sin observaciones");

                // Aplicar estilo de datos a todas las celdas
                for (int i = 0; i < headers.length; i++) {
                    if (i != 11) { // Excluir celda de probabilidad (ya tiene estilo)
                        dataRow.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }

            // ========== RESUMEN ESTADÍSTICO ==========

            int summaryStartRow = rowNum + 2;

            // Título del resumen
            Row summaryTitleRow = sheet.createRow(summaryStartRow);
            Cell summaryTitleCell = summaryTitleRow.createCell(0);
            summaryTitleCell.setCellValue("RESUMEN ESTADÍSTICO");
            summaryTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(summaryStartRow, summaryStartRow, 0, 13));

            // Estadísticas
            String[] summaryLabels = {
                    "Total de Usuarios Potenciales:",
                    "Probabilidad Promedio:",
                    "Usuarios con Probabilidad Alta (>80%):",
                    "Usuarios con Probabilidad Media (60-80%):",
                    "Usuarios con Probabilidad Baja (<60%):",
                    "Tasa de Conversión Estimada:"
            };

            String[] summaryValues = calcularEstadisticas(usuariosProcesados);

            for (int i = 0; i < summaryLabels.length; i++) {
                Row summaryRow = sheet.createRow(summaryStartRow + i + 1);
                summaryRow.createCell(0).setCellValue(summaryLabels[i]);
                summaryRow.createCell(1).setCellValue(summaryValues[i]);

                // Aplicar estilo al label
                summaryRow.getCell(0).setCellStyle(summaryStyle);

                // Aplicar estilo al valor
                Cell valueCell = summaryRow.getCell(1);
                valueCell.setCellStyle(summaryStyle);
            }

            // ========== AJUSTES FINALES ==========

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Congelar paneles (títulos y encabezados visibles al desplazar)
            sheet.createFreezePane(0, 5, 0, 5);

            // ========== GENERAR ARCHIVO ==========

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

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

    // Método para calcular estadísticas
    private String[] calcularEstadisticas(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            return new String[]{"0", "0%", "0", "0", "0", "0%"};
        }

        double promedioProb = usuarios.stream()
                .mapToDouble(u -> u.getProbabilidad() != null ? u.getProbabilidad() : 0)
                .average()
                .orElse(0);

        long altaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 80)
                .count();

        long mediaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 60 && u.getProbabilidad() < 80)
                .count();

        long bajaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() < 60)
                .count();

        double tasaConversion = (altaProb * 0.8) + (mediaProb * 0.5) + (bajaProb * 0.2);

        return new String[]{
                String.valueOf(usuarios.size()),
                String.format("%.1f%%", promedioProb),
                String.valueOf(altaProb),
                String.valueOf(mediaProb),
                String.valueOf(bajaProb),
                String.format("%.1f%%", tasaConversion)
        };
    }

    // Método auxiliar para nivel de confianza mejorado
    private String determinarNivelConfianza(double probabilidad) {
        if (probabilidad >= 80) return "⭐ MUY ALTO";
        if (probabilidad >= 60) return "▲ ALTO";
        if (probabilidad >= 40) return "● MEDIO";
        return "○ BAJO";
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

    private void agregarEstadisticasResumen(Sheet sheet, List<Usuario> usuarios, int startRow) {
        if (usuarios.isEmpty()) return;

        // Fila separadora
        org.apache.poi.ss.usermodel.Row rowSeparator = sheet.createRow(startRow++);

        // Título del resumen
        org.apache.poi.ss.usermodel.Row rowResumen = sheet.createRow(startRow++);
        rowResumen.createCell(0).setCellValue("RESUMEN ESTADÍSTICO");

        // Total usuarios
        org.apache.poi.ss.usermodel.Row rowTotal = sheet.createRow(startRow++);
        rowTotal.createCell(0).setCellValue("Total usuarios potenciales:");
        rowTotal.createCell(1).setCellValue(usuarios.size());

        // Calcular promedio de probabilidad
        double promedioProb = usuarios.stream()
                .mapToDouble(u -> u.getProbabilidad() != null ? u.getProbabilidad() : 0)
                .average()
                .orElse(0);

        org.apache.poi.ss.usermodel.Row rowPromedio = sheet.createRow(startRow++);
        rowPromedio.createCell(0).setCellValue("Probabilidad promedio:");
        rowPromedio.createCell(1).setCellValue(String.format("%.1f%%", promedioProb));

        // Usuarios con alta probabilidad (>80%)
        long altaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 80)
                .count();

        org.apache.poi.ss.usermodel.Row rowAltaProb = sheet.createRow(startRow++);
        rowAltaProb.createCell(0).setCellValue("Usuarios con probabilidad alta (>80%):");
        rowAltaProb.createCell(1).setCellValue(altaProb);

        // Usuarios con probabilidad media (60-80%)
        long mediaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 60 && u.getProbabilidad() < 80)
                .count();

        org.apache.poi.ss.usermodel.Row rowMediaProb = sheet.createRow(startRow++);
        rowMediaProb.createCell(0).setCellValue("Usuarios con probabilidad media (60-80%):");
        rowMediaProb.createCell(1).setCellValue(mediaProb);
    }


}