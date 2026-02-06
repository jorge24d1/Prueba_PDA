package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/n8n")
@CrossOrigin(origins = "*") // Permitir acceso desde cualquier lugar (necesario para n8n/ngrok)
public class N8nRestController {

    @Autowired
    private VehiculoService vehiculoService;

    // Endpoint principal para búsqueda de vehículos
    @GetMapping("/vehiculos")
    public ResponseEntity<?> buscarVehiculos(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer anioMin,
            @RequestParam(required = false) Integer anioMax,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String categoria
    ) {
        try {
            System.out.println("🤖 [N8N] Solicitud de búsqueda recibida:");
            System.out.println("   - Marca: " + marca);
            System.out.println("   - Modelo: " + modelo);
            System.out.println("   - Año: " + anioMin + " - " + anioMax);
            
            List<Vehiculo> resultados = vehiculoService.buscarVehiculos(
                    marca, modelo, anioMin, anioMax, precioMin, precioMax, categoria
            );

            // Mapeamos a un formato JSON limpio y garantizado
            List<Map<String, Object>> respuesta = resultados.stream().map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", v.getId());
                map.put("marca", v.getMarca());
                map.put("modelo", v.getModelo());
                map.put("anio", v.getAño());
                map.put("precio", v.getPrecio());
                map.put("categoria", v.getCategoria());
                map.put("combustible", v.getCombustible());
                map.put("transmision", v.getTransmision());
                map.put("descripcion", v.getDescripcion());
                // Agregamos URL de imagen si existe
                map.put("imagen", v.getImagenUrl());
                return map;
            }).collect(Collectors.toList());

            System.out.println("✅ [N8N] Encontrados " + respuesta.size() + " vehículos.");
            
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno al procesar la búsqueda", "detalle", e.getMessage()));
        }
    }
    
    // Endpoint de prueba para verificar conectividad
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "online", 
            "message", "Conexión exitosa con el backend local",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
