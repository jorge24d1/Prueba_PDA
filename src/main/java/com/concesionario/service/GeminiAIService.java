package com.concesionario.service;

import com.concesionario.dto.GeminiRequest;
import com.concesionario.dto.GeminiResponse;
import com.concesionario.model.Vehiculo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class GeminiAIService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public GeminiAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * ✅ MÉTODO MEJORADO: Con detección de alternativas
     */
    /**
     * ✅ MÉTODO MEJORADO: Prompt optimizado para Venta Consultiva
     */
    public Mono<String> analizarYSeleccionarVehiculo(String mensajeUsuario, List<Vehiculo> vehiculosDisponibles) {

        if (vehiculosDisponibles.isEmpty()) {
            return Mono.just("🤖 **Dante**: Lo siento, no tengo vehículos disponibles en este momento. Por favor contacta a un asesor humano.");
        }

        String infoVehiculos = construirInfoVehiculos(vehiculosDisponibles);

        // ✅ DETECTAR INTENCIÓN BÁSICA
        boolean esAlternativa = mensajeUsuario.toLowerCase().matches(".*(otro|otra|diferente|más|mas|opción|opcion|ver más).*");
        
        // PROMPT DE ALTO RENDIMIENTO PARA VENTA (Roleplay: Asesor Experto)
        String prompt = "Eres Dante, el asesor experto de 'NextGen Motors'. Tu meta es vender autos siendo útil, persuasivo y amable.\n\n" +

                "INVENTARIO ACTUAL (Solo ofrece estos autos):\n" + infoVehiculos + "\n\n" +

                "USUARIO DICE: \"" + mensajeUsuario + "\"\n\n" +
                
                "TUS INSTRUCCIONES:\n" +
                "1. ANALIZA qué busca el usuario (familia, velocidad, economía, etc.). Si no es claro, usa tu intuición basada en lo que escribe.\n" +
                "2. SELECCIONA el MEJOR vehículo del inventario para él.\n" +
                (esAlternativa ? 
                "   IMPORTANTE: El usuario quiere ver OTRAS opciones. Elige uno distinto al que probablemente ya vio.\n" :
                "   Elige la opción más relevante y atractiva.\n") +
                "3. VENDE EL AUTO: Describe por qué es perfecto para él. Menciona marca, modelo y una característica clave (motor, espacio, etc.).\n" +
                "4. SÉ BREVE Y AMABLE. No hagas listas largas. Habla como un humano.\n" +
                "5. Si NINGÚN auto encaja bien, sé honesto y sugiere el que más se acerque, explicando por qué.\n\n" +

                "FORMATO DE RESPUESTA OBLIGATORIO:\n" +
                "Empieza con '🤖 **Dante**:' y luego tu mensaje.\n" +
                "Ejemplo: '🤖 **Dante**: ¡Hola! Si buscas espacio, el Toyota Fortuner es ideal...'";

        System.out.println("🤖 Dante AI - Prompt Generado (Inventario: " + vehiculosDisponibles.size() + " autos)");

        GeminiRequest request = crearRequest(prompt);

        return webClient.post()
                .uri(GEMINI_API_URL + "?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(this::extraerTextoRespuesta)
                .onErrorResume(e -> {
                    System.err.println("❌ Error llamada Gemini: " + e.getMessage());
                    return Mono.just("🤖 **Dante**: Tuve un pequeño problema técnico pensando tu respuesta. ¿Me das un segundo y me repites qué buscabas?");
                });
    }

    private String construirInfoVehiculos(List<Vehiculo> vehiculos) {
        StringBuilder sb = new StringBuilder();
        int maxVehiculos = Math.min(vehiculos.size(), 25); // Limitar contexto para no exceder tokens gratis si hay muchos
        
        for (int i = 0; i < maxVehiculos; i++) {
            Vehiculo v = vehiculos.get(i);
            sb.append("- ID:").append(v.getId()) // ID útil si quisiéramos links predecibles
              .append(" | ").append(v.getMarca()).append(" ").append(v.getModelo())
              .append(" (").append(v.getAño()).append(")")
              .append(" | $").append(String.format("%,.0f", v.getPrecio()))
              .append(" | ").append(v.getCategoria())
              .append(" | ").append(v.getCombustible())
              .append(" | Trans: ").append(v.getTransmision())
              .append("\n");
        }
        return sb.toString();
    }

    private GeminiRequest crearRequest(String prompt) {
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);

        content.setParts(List.of(part));
        request.setContents(List.of(content));
        
        // Ajustes de generación para respuestas más creativas pero controladas
        // (Opcional, si tu DTO lo soporta. Si no, lo dejamos básico)
        return request;
    }

    private String extraerTextoRespuesta(GeminiResponse response) {
        if (response.getCandidates() != null && !response.getCandidates().isEmpty() &&
            response.getCandidates().get(0).getContent() != null &&
            response.getCandidates().get(0).getContent().getParts() != null) {

            String respuesta = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            
            // Limpieza básica
            return respuesta.trim();
        }
        return "🤖 **Dante**: No se me ocurre nada en este momento. ¿Podemos intentar otra búsqueda?";
    }
}