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
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";



    public GeminiAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * ✅ MÉTODO NUEVO: Analizar y seleccionar el mejor vehículo
     */
    public Mono<String> analizarYSeleccionarVehiculo(String mensajeUsuario, List<Vehiculo> vehiculosDisponibles) {

        if (vehiculosDisponibles.isEmpty()) {
            return Mono.just("🤖 **Dante**: No encontré vehículos que coincidan exactamente con tu búsqueda. ¿Podrías ser más específico? Por ejemplo: 'SUV familiar', 'auto económico'.");
        }

        String infoVehiculos = construirInfoVehiculos(vehiculosDisponibles);

        String prompt = "Eres Dante, un asistente especializado en vehículos de concesionario.\n\n" +
                "MENSAJE DEL USUARIO: \"" + mensajeUsuario + "\"\n\n" +
                "VEHÍCULOS DISPONIBLES EN EL CONCESIONARIO:\n" + infoVehiculos + "\n\n" +
                "INSTRUCCIONES CRÍTICAS:\n" +
                "1. Analiza EXACTAMENTE qué busca el usuario\n" +
                "2. SELECCIONA SOLO 1 VEHÍCULO que mejor se adapte a lo que pide\n" +
                "3. Si menciona una marca/modelo específico, prioriza ese\n" +
                "4. Responde de forma NATURAL y ENTUSIASTA\n" +
                "5. Incluye detalles específicos del vehículo seleccionado\n" +
                "6. NO inventes vehículos que no estén en la lista\n" +
                "7. MÁXIMO 1 recomendación\n\n" +
                "FORMATO DE RESPUESTA:\n" +
                "Comienza con: '🤖 **Dante**: [tu respuesta aquí]'\n" +
                "Incluye: modelo, año, precio, características principales\n" +
                "Termina invitando a explorar más detalles";

        GeminiRequest request = crearRequest(prompt);

        return webClient.post()
                .uri(GEMINI_API_URL + "?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(this::extraerTextoRespuesta)
                .onErrorReturn("🤖 **Dante**: ¡Encontré un vehículo que podría interesarte! Basándome en tu búsqueda, te recomiendo explorar esta opción.");
    }

    private String construirInfoVehiculos(List<Vehiculo> vehiculos) {
        StringBuilder sb = new StringBuilder();
        for (Vehiculo v : vehiculos) {
            sb.append("• ").append(v.getMarca()).append(" ").append(v.getModelo())
                    .append(" | Año: ").append(v.getAño())
                    .append(" | Precio: $").append(String.format("%,.0f", v.getPrecio()))
                    .append(" | Categoría: ").append(v.getCategoria())
                    .append(" | Combustible: ").append(v.getCombustible() != null ? v.getCombustible() : "Gasolina")
                    .append(" | Pasajeros: ").append(v.getPasajeros() != null ? v.getPasajeros() : "N/A")
                    .append(" | Transmisión: ").append(v.getTransmision() != null ? v.getTransmision() : "N/A")
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

        return request;
    }

    private String extraerTextoRespuesta(GeminiResponse response) {
        if (response.getCandidates() != null &&
                !response.getCandidates().isEmpty() &&
                response.getCandidates().get(0).getContent() != null &&
                response.getCandidates().get(0).getContent().getParts() != null &&
                !response.getCandidates().get(0).getContent().getParts().isEmpty()) {

            String respuesta = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            // ✅ Asegurar que la respuesta tenga el formato correcto
            if (!respuesta.contains("🤖 **Dante**:") && !respuesta.startsWith("🤖")) {
                respuesta = "🤖 **Dante**: " + respuesta;
            }
            return respuesta;
        }
        return "🤖 **Dante**: ¡Encontré un vehículo que coincide con tu búsqueda! Te recomiendo explorar esta opción.";
    }
}