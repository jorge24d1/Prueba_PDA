package com.concesionario.service;

import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * @param userId ID del usuario destinatario
     * @param titulo Título de la notificación
     * @param mensaje Cuerpo del mensaje
     */
    public void enviarNotificacion(String userId, String titulo, String mensaje) {
        try {
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);
            
            if (usuario != null && usuario.getFcmToken() != null && !usuario.getFcmToken().isEmpty()) {
                
                // Construir mensaje FCM
                // Nota: Usamos Message.builder() del SDK de Firebase
                com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                        .setToken(usuario.getFcmToken())
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(titulo)
                                .setBody(mensaje)
                                .build())
                        .putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // Para que Flutter sepa qué hacer al clickear
                        .putData("userId", userId)
                        .build();

                // Enviar
                String response = com.google.firebase.messaging.FirebaseMessaging.getInstance().send(message);
                System.out.println("✅ Notificación enviada exitosamente: " + response);
                
            } else {
                System.out.println("⚠️ Usuario " + userId + " no tiene Token FCM registrado. No se envió notificación.");
            }
        } catch (Exception e) {
            // No lanzar excepción para evitar interrumpir el flujo principal del negocio
            System.err.println("❌ Error enviando notificación Firebase: " + e.getMessage());
            // Sugerencia: Si el error es que no se inicializó la App, es porque falta el JSON
        }
    }
}
