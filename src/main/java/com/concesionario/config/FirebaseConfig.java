package com.concesionario.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Verificar si ya existe una instancia para evitar errores de duplicado
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // El archivo serviceAccountKey.json debe estar en src/main/resources/
                InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                System.err.println("⚠️ ALERTA: No se encontró 'serviceAccountKey.json' en resources.");
                System.err.println("   Las notificaciones Push no funcionarán hasta que agregues el archivo.");
                // Retornamos null para que la app no se caiga, pero sabiendo que no habrá notificaciones
                return null;
            }
        }
        return FirebaseApp.getInstance();
    }
}
