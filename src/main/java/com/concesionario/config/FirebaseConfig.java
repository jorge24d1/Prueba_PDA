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
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // ⚠️ HARDCODED CREDENTIALS (Temporal para Fix Azure) - KEY ID: 5421a7b3dc
                 String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6rri3BmrZuvCJ\n" +
                        "8GaauhKTc16h4pGGS2Oem1pArQwb9i9JDVwr79YM3GG5M/G0zwJ8JwWlsMvxDNwE\n" +
                        "qrHHZoLtTBlgaOMxmUTFOisqVIt9fYw4kvFATdQNplXy2lzKcHBI4obpRG6LxmsK\n" +
                        "/ndRGeiN23CwIuMwboCRPaHkNKgdOZtJ3YA5oozFu7m3Fr/fhSVRXQ8rER+ItsDw\n" +
                        "FI+9QXIiItvYAMAA9DT462HxDdKs3fDLWADPfT/fa3x/dCyqUOTeh1eKKFPYwHXy\n" +
                        "luyXjSHB+ZG/9SVOjMdnsvpJ1Ow14LSm+DnyouaOUj8xHEEhUjSWq46d6yi/0p9x\n" +
                        "WImqvgNRAgMBAAECggEAUMbIY12R/2tNqIoOg5XO8uFhSa/1IwRc8ySqfUDDdBfm\n" +
                        "pvfycYqB9qenW1FYHVHTc/AYl/SzkjZrv1MywKKhNVePxXEtGWmF4cdFJThW8TJR\n" +
                        "aatfW2Exx8Q4/W+x5eCYHVlzYdz77lwpNiUccMnCoBsbHAPEhVDTLcVa1xlXwujL\n" +
                        "RNqcrmW9IOy+8zAWif2J/4S8JTvHkHCfjEWxYZuCthcwSP7cvKQxJZrDE+W6apxp\n" +
                        "GX6w/vARlzl+zuq4Q3JddWd+A8XzUA/2WjI/UJfS1oxDTd3kHpWEhGpkOvhwMtOZ\n" +
                        "GCh/Fv5TnpFyT7Qp9bnqSIvMLE61gxnxzd9yMMfYXwKBgQD10sdCcjcSLhZKa1YB\n" +
                        "KAa+EWvRMjx++Q8KM58OWnzXhL9NZKWCUU23zQ8TIhISHDWtwnSHf0vFX7x+T2DO\n" +
                        "vPDNVtQo+1fNsl4eaAMZBxG9yOK5RFrACTRA4sp911ma7JkXc1KBSJKeuao5VWva\n" +
                        "zRsl0tGr2geQJW1pK3KP6GAASwKBgQDCaSwC1CZDmpOqQXczG9yiKdBJDbwRx1uz\n" +
                        "msLm8GOr+6Rz0nJ+czc8G1Ydkzocq6kN6/Ym7R+2QkAAu/+j1d7wgqiQ7wWdUbNY\n" +
                        "6qXwsFf4rBQUe8p6B7Hzr5rf5FPB0BTkq7/ofPLuTOvdyIc5L/4zqUJ1oAl6Prmc\n" +
                        "oSpBWJThUwKBgHnH4QUO9i9ge5SElkfeX3C7yrEJdSTmF8hTCIIg2+YdEnSx3riv\n" +
                        "tJPQ153/6/ni1cjuI7KYop32LrhYdLoWqijf0h03Xyk9KxlfJ+G3OdfOdFuZ/GdU\n" +
                        "Z/mZIP20uORiP/l5HS+FF3Xgvu7GXbc4kzFqvoqnacXEdO58IrYeRaj/AoGBAMG4\n" +
                        "/WMgNbTd3lEkhPwHwnZRBj0Vk5d/uVAiYh6v4hIpYjkLto1sj9i0QL6dy6VwJLg7\n" +
                        "rgAh/5ylLZPNwjJm1o2BFSm/FDAsjnOG9q39etvP1P6IUD4DVv7FuKVrp06U44zt\n" +
                        "3AB2KbjuK4bH4H9K5JpkCladj3Qh9uEt86ZD968DAoGAYeRw00uKTT7Cw+Imb6vO\n" +
                        "KVcjlFfyDXgkrbpzmq9uOCIBMA5hLUJfK6kSoAVEoeYQ8q7PLWZkq+R8U4cNGls7\n" +
                        "2x1aACHx4V8rXF8v8hNtAKli0iV3k7OtzhS3hlFW0uDwhA+oDFuDdmCttlmq+65x\n" +
                        "DiTwL5Bp7QJfAytTORMGWY0=\n" +
                        "-----END PRIVATE KEY-----";

                 java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
                 jsonMap.put("type", "service_account");
                 jsonMap.put("project_id", "nextgen-c9f08");
                 jsonMap.put("private_key_id", "5421a7b3dc0a6054d154c08f1dae96ae57826009");
                 jsonMap.put("private_key", privateKey);
                 jsonMap.put("client_email", "firebase-adminsdk-fbsvc@nextgen-c9f08.iam.gserviceaccount.com");
                 jsonMap.put("client_id", "110990985312254694410");
                 
                 com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                 String jsonString = mapper.writeValueAsString(jsonMap);
                 InputStream serviceAccount = new java.io.ByteArrayInputStream(jsonString.getBytes());

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                System.err.println("⚠️ ALERTA: Error inicializando Firebase Hardcoded.");
                e.printStackTrace();
                return null;
            }
        }
        return FirebaseApp.getInstance();
    }
}
