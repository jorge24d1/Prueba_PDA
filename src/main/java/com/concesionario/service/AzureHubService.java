package com.concesionario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class AzureHubService {

    @Value("${azure.notification-hub.connection-string}")
    private String connectionString;

    @Value("${azure.notification-hub.hub-name}")
    private String hubName;

    private final WebClient webClient;

    public AzureHubService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void sendNotification(String jsonBody, String tag) {
        try {
            // Clean inputs to avoid 401 Unauthorized due to hidden spaces
            connectionString = connectionString.trim();
            hubName = hubName.trim();

            // Parse connection string
            String endpoint = getValueFromConnectionString(connectionString, "Endpoint");
            String sasKeyName = getValueFromConnectionString(connectionString, "SharedAccessKeyName");
            String sasKey = getValueFromConnectionString(connectionString, "SharedAccessKey");

            // Prepare URL
            // Format: https://<namespace>.servicebus.windows.net/<hub-name>/messages/?api-version=2015-01
            String cleanEndpoint = endpoint.replace("sb://", "https://").replaceAll("/$", "");
            String uri = cleanEndpoint + "/" + hubName + "/messages/?api-version=2015-01";
            
            // Generate SAS Token
            String sasToken = generateSasToken(cleanEndpoint + "/" + hubName, sasKeyName, sasKey);

            // Send Request
            // Header "ServiceBusNotification-Format": "gcm" for FCM v1/Legacy (Azure uses 'gcm' for Google/Firebase)
            // Header "ServiceBusNotification-Tags": tag (if targeting specific user logic)
            // For Direct Send (no registration), we use "ServiceBusNotification-DeviceHandle" header if targeting a token directly.
            
            // However, the standard "Send" API requires a tag or uses broadcast.
            // If we want to send to a SPECIFIC DEVICE TOKEN (Direct Send), we use the specific header:
            // "ServiceBusNotification-DeviceHandle": <fcm_token>
            
            System.out.println("🚀 Enviando notificación a Azure Hub: " + hubName);
            
            String response = webClient.post()
                    .uri(uri)
                    .header("Authorization", sasToken)
                    .header("Content-Type", "application/json;charset=utf-8")
                    .header("ServiceBusNotification-Format", "gcm") 
                    .header("ServiceBusNotification-DeviceHandle", tag) // Direct Send to Token
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Block for sync execution to match previous logic
            
            System.out.println("✅ Notificación enviada. Azure Response: " + (response == null ? "OK (201)" : response));

        } catch (Exception e) {
            System.err.println("❌ Error enviando a Azure REST API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateSasToken(String resourceUri, String keyName, String key) {
        try {
            long epoch = Instant.now().getEpochSecond() + 3600; // Expires in 1 hour
            String encodedUri = URLEncoder.encode(resourceUri, StandardCharsets.UTF_8).toLowerCase();
            String signatureToSign = encodedUri + "\n" + epoch;

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] signatureBytes = hmac.doFinal(signatureToSign.getBytes(StandardCharsets.UTF_8));
            String manualSignature = Base64.getEncoder().encodeToString(signatureBytes);
            String encodedSignature = URLEncoder.encode(manualSignature, StandardCharsets.UTF_8);

            return "SharedAccessSignature sr=" + encodedUri + "&sig=" + encodedSignature + "&se=" + epoch + "&skn=" + keyName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SAS token", e);
        }
    }

    private String getValueFromConnectionString(String connectionString, String key) {
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            if (part.startsWith(key + "=")) {
                return part.substring(key.length() + 1);
            }
        }
        return null;
    }
}
