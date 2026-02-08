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

    // ✅ NUEVAS VARIABLES PARA ROTACIÓN
    private String ultimaBusqueda;
    private int indiceRotacion = 0;
    private Vehiculo ultimoVehiculoRecomendado;

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

            // ✅ CUARTO: (MODIFICADO) YA NO USAMOS GEMINI LOCAL
            // String respuestaGemini = geminiAIService.analizarYSeleccionarVehiculo(mensajeUsuario, vehiculosFiltrados).block();

            // En su lugar, seleccionamos uno por rotación y devolvemos texto genérico
            Vehiculo vehiculoSeleccionado = seleccionarMejorVehiculo(vehiculosFiltrados, mensajeUsuario);
            
            String respuestaSimple;
            if (vehiculoSeleccionado != null) {
                respuestaSimple = generarRespuestaVehiculo(mensajeUsuario, vehiculoSeleccionado);
            } else {
                 respuestaSimple = "🤖 **Dante**: No encontré un vehículo exacto, pero revisa estas opciones.";
            }

            List<Vehiculo> vehiculoUnico = vehiculoSeleccionado != null ?
                    List.of(vehiculoSeleccionado) : List.of();

            return new RecomendacionResponse(respuestaSimple, vehiculoUnico);

        } catch (Exception e) {
            System.out.println("❌ Error en procesarRecomendacion: " + e.getMessage());
            e.printStackTrace();
            return new RecomendacionResponse(
                    "🤖 **Dante**: ¿Qué tipo de vehículo te interesa? Puedo ayudarte a encontrar autos, SUVs, camionetas...",
                    List.of()
            );
        }
    }

    /**
     * ✅ FILTRAR VEHÍCULOS (Versión simplificada para dar Contexto a Gemini)
     * En lugar de filtrar estrictamente, pasamos los vehículos al LLM para que él decida.
     * Si son muchos, podríamos limitar a los 20 más relevantes por palabras clave, 
     * pero para un inventario manejable, mejor pasar contexto amplio.
     */
    private List<Vehiculo> filtrarVehiculosBasico(List<Vehiculo> vehiculos, String mensaje) {
        // Para máxima "inteligencia", le damos al modelo casi todo y dejamos que él razone.
        // Solo filtramos si la lista es enorme, pero aquí priorizamos la capacidad semántica de Gemini.
        
        // Si hay más de 20 vehículos, hacemos un filtro ligero por coincidencia de texto muy básica
        if (vehiculos.size() > 20) {
            String mensajeLower = mensaje.toLowerCase();
            return vehiculos.stream()
                .sorted((v1, v2) -> {
                    // Ordenar por relevancia simple para cortar los menos probables
                    int s1 = calcularPuntaje(v1, mensajeLower);
                    int s2 = calcularPuntaje(v2, mensajeLower);
                    return Integer.compare(s2, s1);
                })
                .limit(15) // Pasar los top 15 al prompt
                .collect(Collectors.toList());
        }
        
        // Si son pocos, pasamos todos para que Gemini tenga vision global
        return vehiculos;
    }

    /**
     * ✅ Helper para `filtrarVehiculosBasico`
     * Calcula el puntaje de relevancia.
     */
    private int calcularPuntaje(Vehiculo vehiculo, String mensajeLower) {
        int puntaje = 0;

        // Prioridad 1: Categoría (La más alta como pidió el usuario)
        if (coincideCategoria(vehiculo, mensajeLower)) {
            puntaje += 6;
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
     * ✅ NUEVO: Selección con rotación para variedad
     */
    private Vehiculo seleccionarMejorVehiculo(List<Vehiculo> vehiculosFiltrados, String mensaje) {
        return seleccionarVehiculoConRotacion(vehiculosFiltrados, mensaje);
    }

    /**
     * ✅ NUEVO MÉTODO: Sistema de rotación para "otro vehículo"
     */
    private Vehiculo seleccionarVehiculoConRotacion(List<Vehiculo> vehiculosFiltrados, String mensajeUsuario) {
        if (vehiculosFiltrados.isEmpty()) return null;

        String mensajeLower = mensajeUsuario.toLowerCase();

        // Detectar si es búsqueda de "otro"
        boolean esBusquedaDeOtro = esBusquedaDeAlternativa(mensajeLower);

        if (!esBusquedaDeOtro) {
            // Primera búsqueda: reiniciar y tomar el mejor
            indiceRotacion = 0;
            ultimaBusqueda = mensajeLower;
            ultimoVehiculoRecomendado = vehiculosFiltrados.get(0);

            System.out.println("🔄 PRIMERA BÚSQUEDA: Índice " + indiceRotacion +
                    " -> " + ultimoVehiculoRecomendado.getMarca() + " " + ultimoVehiculoRecomendado.getModelo());

            return ultimoVehiculoRecomendado;
        }

        // Búsqueda de "otro": rotar al siguiente
        indiceRotacion++;
        int indice = indiceRotacion % vehiculosFiltrados.size();

        Vehiculo seleccionado = vehiculosFiltrados.get(indice);
        ultimoVehiculoRecomendado = seleccionado;

        System.out.println("🔄 ROTACIÓN: Índice " + indice + " de " + vehiculosFiltrados.size() +
                " vehículos -> " + seleccionado.getMarca() + " " + seleccionado.getModelo());

        return seleccionado;
    }

    /**
     * ✅ NUEVO MÉTODO: Detectar si el usuario pide "otro" vehículo
     */
    private boolean esBusquedaDeAlternativa(String mensajeLower) {
        String[] palabrasAlternativa = {
                "otro", "otra", "diferente", "variedad", "alternativa",
                "opción", "opcion", "similar pero", "muestra otro",
                "qué más", "que mas", "otros", "otras", "no ese", "no me gusta"
        };

        for (String palabra : palabrasAlternativa) {
            if (mensajeLower.contains(palabra)) {
                return true;
            }
        }
        return false;
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