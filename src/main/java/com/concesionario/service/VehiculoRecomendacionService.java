package com.concesionario.service;

import com.concesionario.dto.RecomendacionResponse;
import com.concesionario.model.Vehiculo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculoRecomendacionService {

    private final VehiculoService vehiculoService;
    private final GeminiAIService geminiAIService;

    private static final int MINIMUM_SCORE_THRESHOLD = 5;

    // Helper DTO (inner class) para guardar el puntaje
    private static class VehiculoConPuntaje {
        Vehiculo vehiculo;
        int puntaje;
        VehiculoConPuntaje(Vehiculo v, int p) { this.vehiculo = v; this.puntaje = p; }
    }


    public VehiculoRecomendacionService(VehiculoService vehiculoService,
                                        GeminiAIService geminiAIService) {
        this.vehiculoService = vehiculoService;
        this.geminiAIService = geminiAIService;
    }

    public RecomendacionResponse procesarRecomendacion(String mensajeUsuario) {
        try {
            if (!esSolicitudDeVehiculo(mensajeUsuario)) {
                return generarRespuestaGeneral(mensajeUsuario);
            }

            // ✅ SEGUNDO: Buscar vehículos (Con lógica de SCORING + UMBRAL)
            List<Vehiculo> todosVehiculos = vehiculoService.obtenerTodos();
            List<Vehiculo> vehiculosFiltrados = filtrarVehiculosBasico(todosVehiculos, mensajeUsuario);

            // ✅ TERCERO: REVISAR SI HUBO RESULTADOS (REQUISITO DEL USUARIO)
            if (vehiculosFiltrados.isEmpty()) {
                System.out.println("🚫 No se superó el umbral. Devolviendo 'No encontrado'.");
                return new RecomendacionResponse(
                        "🤖 **Dante**: Lo siento, no encontré ningún vehículo que coincida con tu búsqueda. ¿Podrías ser más específico? (ej: 'SUV familiar', 'auto económico')",
                        List.of()
                );
            }

            // ✅ CUARTO: USAR GEMINI (Solo si hay vehículos relevantes)
            String respuestaGemini = geminiAIService.analizarYSeleccionarVehiculo(mensajeUsuario, vehiculosFiltrados)
                    .block();

            // ✅ QUINTO: Seleccionar el vehículo más relevante (El de mayor puntaje)
            Vehiculo vehiculoSeleccionado = seleccionarMejorVehiculo(vehiculosFiltrados, mensajeUsuario);

            List<Vehiculo> vehiculoUnico = vehiculoSeleccionado != null ?
                    List.of(vehiculoSeleccionado) : List.of();

            return new RecomendacionResponse(respuestaGemini, vehiculoUnico);

        } catch (Exception e) {
            return new RecomendacionResponse(
                    "🤖 **Dante**: ¿Qué tipo de vehículo te interesa? Puedo ayudarte a encontrar autos, SUVs, camionetas...",
                    List.of()
            );
        }
    }

    /**
     * ✅ FILTRAR VEHÍCULOS (SOLUCIÓN CON SCORING + UMBRAL)
     */
    private List<Vehiculo> filtrarVehiculosBasico(List<Vehiculo> vehiculos, String mensaje) {
        String mensajeLower = mensaje.toLowerCase();
        System.out.println("🔍 BUSCANDO (v3 con Umbral): '" + mensaje + "' -> '" + mensajeLower + "'");

        // 1. Calcular puntaje para CADA vehículo
        List<VehiculoConPuntaje> vehiculosConPuntaje = vehiculos.stream()
                .map(vehiculo -> {
                    int puntaje = calcularPuntaje(vehiculo, mensajeLower);
                    return new VehiculoConPuntaje(vehiculo, puntaje);
                })
                .filter(vp -> vp.puntaje >= MINIMUM_SCORE_THRESHOLD) // <-- REQUISITO CLAVE: UMBRAL MÍNIMO
                .sorted((vp1, vp2) -> Integer.compare(vp2.puntaje, vp1.puntaje)) // Ordenar por puntaje DESC
                .collect(Collectors.toList());

        // 2. Si NADA superó el umbral, la lista estará vacía
        // [SE ELIMINÓ EL FALLBACK A DESTACADOS]
        if (vehiculosConPuntaje.isEmpty()) {
            System.out.println("🚫 No se encontraron vehículos con el puntaje mínimo (" + MINIMUM_SCORE_THRESHOLD + ").");
            return List.of(); // Devolver lista vacía
        }

        // 3. Convertir de nuevo a List<Vehiculo> (los 5 mejores)
        List<Vehiculo> filtrados = vehiculosConPuntaje.stream()
                .map(vp -> vp.vehiculo)
                .limit(5)
                .collect(Collectors.toList());

        System.out.println("📊 RESULTADOS FILTRADOS (Umbral " + MINIMUM_SCORE_THRESHOLD + "): " + filtrados.size() + " vehículos");
        return filtrados;
    }

    /**
     * ✅ Helper para `filtrarVehiculosBasico`
     * Calcula el puntaje de relevancia.
     */
    private int calcularPuntaje(Vehiculo vehiculo, String mensajeLower) {
        int puntaje = 0;

        // Prioridad 1: Categoría (La más alta como pidió el usuario)
        if (coincideCategoria(vehiculo, mensajeLower)) {
            puntaje += 6; // <-- Prioridad más alta
        }

        // Prioridad 2: Marca/Modelo
        if (vehiculo.getMarca() != null && mensajeLower.contains(vehiculo.getMarca().toLowerCase())) {
            System.out.println("  [+5] Coincide Marca: " + vehiculo.getMarca());
            puntaje += 5;
        }
        if (vehiculo.getModelo() != null && mensajeLower.contains(vehiculo.getModelo().toLowerCase())) {
            System.out.println("  [+5] Coincide Modelo: " + vehiculo.getModelo());
            puntaje += 5;
        }

        // Prioridad 3: Atributos secundarios
        if (vehiculo.getCombustible() != null && mensajeLower.contains(vehiculo.getCombustible().toLowerCase())) {
            System.out.println("  [+2] Coincide Combustible: " + vehiculo.getCombustible());
            puntaje += 2;
        }
        if (vehiculo.getTransmision() != null && mensajeLower.contains(vehiculo.getTransmision().toLowerCase())) {
            System.out.println("  [+1] Coincide Transmisión: " + vehiculo.getTransmision());
            puntaje += 1;
        }

        // [SE ELIMINÓ 'isDestacado()' DE AQUÍ]
        return puntaje;
    }

    /**
     * ✅ Helper centralizado para verificar categorías (MEJORADO)
     */
    private boolean coincideCategoria(Vehiculo vehiculo, String mensajeLower) {
        if (vehiculo.getCategoria() == null) {
            return false;
        }
        String categoriaLower = vehiculo.getCategoria().toLowerCase();

        // "camioneta" puede ser SUV o Pick-Up
        if (mensajeLower.contains("camioneta") && (categoriaLower.contains("suv") || categoriaLower.contains("pick-up"))) {
            System.out.println("  [+6] Coincide (camioneta): " + categoriaLower);
            return true;
        }

        // Búsqueda directa
        if ((mensajeLower.contains("comercial") && categoriaLower.contains("comercial")) ||
                (mensajeLower.contains("automóvil") && categoriaLower.contains("automóvil")) ||
                (mensajeLower.contains("automovil") && categoriaLower.contains("automóvil")) ||
                (mensajeLower.contains("pick-up") && categoriaLower.contains("pick-up")) ||
                (mensajeLower.contains("suv") && categoriaLower.contains("suv")) ||
                (mensajeLower.contains("deportivo") && categoriaLower.contains("deportivo")) ||
                (mensajeLower.contains("performance") && categoriaLower.contains("performance")) ||
                (mensajeLower.contains("híbrido") && categoriaLower.contains("híbrido")) ||
                (mensajeLower.contains("hibrido") && categoriaLower.contains("híbrido"))) {

            System.out.println("  [+6] Coincide (categoría): " + categoriaLower);
            return true;
        }

        return false;
    }


    /**
     * ✅ SELECCIONAR EL MEJOR VEHÍCULO (SIMPLIFICADO)
     * Confía en el scoring: el mejor auto es simplemente el primero de la lista.
     */
    private Vehiculo seleccionarMejorVehiculo(List<Vehiculo> vehiculosFiltrados, String mensaje) {
        // La lista 'vehiculosFiltrados' NUNCA estará vacía aquí (se comprueba antes)
        // y YA VIENE ORDENADA por puntaje.
        Vehiculo mejorOpcion = vehiculosFiltrados.get(0);
        System.out.println("⭐ Selección (Mejor Puntaje): " + mejorOpcion.getMarca() + " " + mejorOpcion.getModelo());
        return mejorOpcion;
    }


    // --- [MÉTODOS SIN CAMBIOS] ---

    private boolean esSolicitudDeVehiculo(String mensaje) {
        String mensajeLower = mensaje.toLowerCase();

        String[] palabrasVehiculo = {
                "auto", "carro", "vehículo", "coche", "moto", "suv", "camioneta",
                "pickup", "deportivo", "sedán", "hatchback", "económico", "barato",
                "familiar", "espacioso", "potente", "chevrolet", "toyota", "ford",
                "nissan", "bmw", "mercedes", "audi", "honda", "hyundai", "kia",
                "precio", "comprar", "busco", "quiero", "necesito", "recomienda",
                "sugiere", "encuentra", "modelo", "marca", "gasolina", "diesel",
                "electrico", "hibrido", "manual", "automatica"
        };

        for (String palabra : palabrasVehiculo) {
            if (mensajeLower.contains(palabra)) {
                return true;
            }
        }

        List<Vehiculo> todosVehiculos = vehiculoService.obtenerTodos();
        for (Vehiculo vehiculo : todosVehiculos) {
            if (vehiculo.getMarca() != null &&
                    mensajeLower.contains(vehiculo.getMarca().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private RecomendacionResponse generarRespuestaGeneral(String mensaje) {
        String mensajeLower = mensaje.toLowerCase();
        if (mensajeLower.contains("hola") || mensajeLower.contains("buenos")) {
            return new RecomendacionResponse(
                    "¡Hola! Soy Dante, tu asistente de NextGen Motors. " +
                            "¿Estás buscando algún vehículo en particular?",
                    List.of()
            );
        } else if (mensajeLower.contains("gracias")) {
            return new RecomendacionResponse(
                    "¡De nada! ¿Hay algo más en lo que pueda ayudarte?",
                    List.of()
            );
        } else {
            return new RecomendacionResponse(
                    "¿Te interesa buscar algún vehículo? Puedo ayudarte a encontrar " +
                            "autos, SUVs, camionetas, deportivos... ¿Qué tipo buscas?",
                    List.of()
            );
        }
    }

    private String generarRespuestaVehiculo(String mensajeUsuario, Vehiculo vehiculo) {
        // Este método ya no se usa activamente si Gemini genera toda la respuesta,
        // pero es bueno tenerlo de fallback.
        if (vehiculo == null) {
            return "🤖 **Dante**: No encontré vehículos que coincidan exactamente con tu búsqueda. " +
                    "¿Podrías ser más específico? Por ejemplo: 'SUV familiar', 'auto económico', etc.";
        }
        return "🤖 **Dante**: ¡Perfecto! Encontré este vehículo que coincide con lo que buscas:\n\n" +
                "**" + vehiculo.getMarca() + " " + vehiculo.getModelo() + "**\n" +
                "📅 Año: " + vehiculo.getAño() + " | 💰 $" + String.format("%,.0f", vehiculo.getPrecio()) + "\n" +
                "🚗 " + vehiculo.getCategoria() + " | ⛽ " +
                (vehiculo.getCombustible() != null ? vehiculo.getCombustible() : "Gasolina") + "\n\n" +
                "¿Te gustaría explorar más detalles o ver otros vehículos similares?";
    }
}