package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.model.Cita;
import com.concesionario.service.VehiculoService;
import com.concesionario.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.concesionario.service.VehiculoRecomendacionService;
import com.concesionario.dto.RecomendacionResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VehiculoController {
    private final VehiculoService vehiculoService;
    private final CitaService citaService;
    private final VehiculoRecomendacionService recomendacionService; // ✅ Declarado

    // ✅ CONSTRUCTOR CORREGIDO - inicializar TODOS los servicios
    public VehiculoController(VehiculoService vehiculoService,
                              CitaService citaService,
                              VehiculoRecomendacionService recomendacionService) {
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.recomendacionService = recomendacionService; // ✅ INICIALIZADO
    }

    @GetMapping("/vehiculos")
    public String mostrarVehiculos(Model model) {
        List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();

        // Solución 1: Filtrar vehículos sin categoría
        Map<String, List<Vehiculo>> vehiculosPorCategoria = vehiculos.stream()
                .filter(v -> v.getCategoria() != null && !v.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(Vehiculo::getCategoria));

        model.addAttribute("categorias", vehiculosPorCategoria.keySet());
        model.addAttribute("vehiculosPorCategoria", vehiculosPorCategoria);
        return "vehiculos";
    }

    @GetMapping("/fragments/chatbot")
    public String Inicio(Model model) {
//
        return "chatbot";
    }
    @PostMapping("/api/chatbot/mensaje")
    @ResponseBody
    public ResponseEntity<ChatbotResponse> procesarMensajeChatbot(@RequestBody ChatbotRequest request) {
        try {
            RecomendacionResponse recomendacion = recomendacionService.procesarRecomendacion(request.getMensaje()); // ✅ CORREGIDO

            ChatbotResponse response = new ChatbotResponse();
            response.setRespuesta(recomendacion.getRespuesta());
            response.setVehiculosRecomendados(recomendacion.getVehiculosRecomendados());
            response.setTimestamp(java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ChatbotResponse errorResponse = new ChatbotResponse();
            errorResponse.setRespuesta("Lo siento, hubo un error. Por favor, intenta de nuevo.");
            errorResponse.setTimestamp(java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // DTOs internos del Controller
    public static class ChatbotRequest {
        private String mensaje;
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }

    public static class ChatbotResponse {
        private String respuesta;
        private List<Vehiculo> vehiculosRecomendados;
        private String timestamp;

        public String getRespuesta() { return respuesta; }
        public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
        public List<Vehiculo> getVehiculosRecomendados() { return vehiculosRecomendados; }
        public void setVehiculosRecomendados(List<Vehiculo> vehiculosRecomendados) {
            this.vehiculosRecomendados = vehiculosRecomendados;
        }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }




    @GetMapping("/vehiculos/explorar/{id}")
    public String explorarVehiculo(@PathVariable String id, Model model) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        if (vehiculo == null) {
            return "redirect:/vehiculos";
        }
        model.addAttribute("vehiculo", vehiculo);
        return "explorar-vehiculo";
    }
    @GetMapping("/")
    public String redirectToInicio() {
        return "redirect:/usuario/Inicio";
    }

    @GetMapping("/nosotros")
    public String nosotros(){
        return "nosotros";
    }
    @GetMapping("/garantias")
    public String garantias(){
        return "garantias";
    }
    @GetMapping("/credito")
    public String credito(){
        return "credito";
    }

    @GetMapping("/cookies")
    public String cookies(){
        return "cookies";
    }
    @GetMapping("/terminos")
    public String terminos(){
        return "terminos";
    }
    @GetMapping("/ubicaciones")
    public String ubicaciones(){
        return "ubicaciones";
    }

}