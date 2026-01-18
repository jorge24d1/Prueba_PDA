package com.concesionario.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    // Replace these with your actual Supabase details or inject them via @Value
    private static final String SUPABASE_URL = "https://yszyenbpsmauujxuyiln.supabase.co";
    // NOTE: Using service_role key to ensure write access. In production, use env variables!
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlzenllbmJwc21hdXVqeHV5aWxuIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2ODc1NjIyOSwiZXhwIjoyMDg0MzMyMjI5fQ.RsJuisijWi3GcCrxbVqNlntuzHSZjVRaSMMVofkn34I";
    private static final String BUCKET_NAME = "vehiculos-3d";

    private final RestTemplate restTemplate;

    public SupabaseStorageService() {
        this.restTemplate = new RestTemplate();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replace(" ", "_");
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filename;

        System.out.println("🚀 [Supabase] Iniciando subida de archivo: " + filename);
        System.out.println("📏 [Supabase] Tamaño: " + file.getSize() + " bytes");
        System.out.println("🔗 [Supabase] URL destino: " + uploadUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SUPABASE_KEY);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [Supabase] Subida exitosa: " + filename);
                // Return the public URL
                return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filename;
            } else {
                System.err.println("❌ [Supabase] Falló la subida. Status: " + response.getStatusCode());
                System.err.println("❌ [Supabase] Respuesta: " + response.getBody());
                throw new IOException("Failed to upload to Supabase: " + response.getStatusCode() + " Body: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("🔥 [Supabase] Excepción durante la subida: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error uploading to Supabase: " + e.getMessage());
        }
    }
}
