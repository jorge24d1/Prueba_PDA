package com.concesionario.service;

import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AzureHubService azureHubService;

    /**
     * @param userId ID del usuario destinatario
     * @param titulo Título de la notificación
     * @param mensaje Cuerpo del mensaje
     */
    public void enviarNotificacion(String userId, String titulo, String mensaje) {
        try {
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);
            
            if (usuario != null && usuario.getFcmToken() != null && !usuario.getFcmToken().isEmpty()) {
                
                // Construir mensaje nativo de FCM en formato JSON
                // Azure Notification Hubs recibe el payload de FCM y lo reenvía
                String fcmPayload = "{" +
                        "\"notification\": {" +
                        "    \"title\": \"" + titulo + "\"," +
                        "    \"body\": \"" + mensaje + "\"" +
                        "}," +
                        "\"data\": {" +
                        "    \"click_action\": \"FLUTTER_NOTIFICATION_CLICK\"," +
                        "    \"userId\": \"" + userId + "\"" +
                        "}" +
                        "}";

                // Enviar usando nuestro servicio REST personalizado
                azureHubService.sendNotification(fcmPayload, usuario.getFcmToken());
                
                System.out.println("✅ Notificación enviada exitosamente vía Azure REST a: " + userId);
                
            } else {
                System.out.println("⚠️ Usuario " + userId + " no tiene Token FCM registrado. No se envió notificación.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación Azure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
