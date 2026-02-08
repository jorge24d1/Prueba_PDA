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
                // ⚠️ HARDCODED CREDENTIALS (Updated)
                 String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXzMWmvudEKgou\n" +
                        "eRKC4cLTMobTNa0vVXZo+NFVH7UwCYgWeM8ZVwPElB+wR6QZdvRj8MaFqAva1+MS\n" +
                        "GaixsL7o2tpwf6gR8XpRkxA83AGcz2cXMlhsg6P3BLY+0KtinKwIpoknwz1dCqe1\n" +
                        "HLHV2UDqiJ0v5QxK/H/M0xi9DQ9F5yky2IeVQiLFxEcSeXRvCXm8H8YzQ/RWPW7e\n" +
                        "F9MNBOn4qshpdmZ1bzz2Uj5ToBt59UWqtklridSv2yHFghyCj+qygL5uTKqGzMb3\n" +
                        "lSa6bcFbSjzhH2W7LKGmzAUs0OmnDa+d6YSdQAUXeVxfSz8ihQ2H9ijdbBV4MMaU\n" +
                        "h95QcTnNAgMBAAECggEABY4dLun45U9d31os6ZSWIEvOuqH8kX6LfnHvRIc22+gt\n" +
                        "Y8J4tGZby/DZFrFjJE9scs/X5AJEqiCRExN/Tz627dHnK+DFZ3VdKD5n2gz5viGq\n" +
                        "QHi7haps8K2VIcgEeEUK1/zdiL1QseahlojTSo57HVocg9PaVvrV48qsiBaXQtPm\n" +
                        "qvBFiEygi2rMEVlkxa0/NLTqRbisLqMMR05Soj25zDSABsrlhQlAcHyTWZbnW5sx\n" +
                        "dJeknivF9grqpYzdU6li/0Mqb4vMLE0I0Xvf5cRSYFodwC1CLWT/CpC3GueDzUj4\n" +
                        "UxKO9h4MTHS3QlXtvr0m9xOb1Il8dtpEtc1WAD0x/QKBgQDUCXCEDS9mEeVWjoKG\n" +
                        "pp1kdfo3gHS871LLagiPbnBHuklmWqeYxHN5Yad7rfJ/ThC9Aob0rwolQNVEllc/\n" +
                        "KEqwWUa6uhdrUyMkwcN6h2+NvODiPSRWl8VgkKzlWK3GnJ6YGFVkbUN/xId/q9E4\n" +
                        "XYGh6F5c2qEpMHeKRwre07nyHwKBgQC3Rg4NXKXzB2mUYe+oUp0pKxfBJ43dorPR\n" +
                        "ffU5z230MUFM7XEWWnoF7iZZGTc9eTiRXIfeKsqabOaxejkJ6gJ67bikKoXm2HLB\n" +
                        "koC48IqM7KvOmdbfLXQ66BtRHML1jhHXlFtfIMHN9e+43cL1Fgpj4MkxOKCeLk/t\n" +
                        "1zbtB+KOkwKBgF/jCOvenpG1BBZZifNkg4a7x3Sz9Za/T5zHjal3UvVM7AaKdoIi\n" +
                        "TOoWXEwVBLXHUtB2xRr/0UG5iPhkWH4qNWR8avzkTNTvGtRzf3f4qVzajoQhDD7Q\n" +
                        "kaSyqE1rBeLsTtgPIQayVrT/5hq5U3r+5jGWyJ2LDRh4X4a7y9Gqw8sXAoGBAIWx\n" +
                        "1Q5qR6TCA4L67pkbqwodJXcCI7tKtWJdQEarysoYJEq564Ui+2DYGaFlg6aUh2+C\n" +
                        "0JqBHMsKJj/RqUpSKv0B7W/UlVmRDCWpeb1vu6KSA8Ly9oxX3DIcn9DL6my29s0M\n" +
                        "rJr4imh0wCtbmpyvN2Uk1Z/Sb1j079nQ5YCEAAGBAoGAUGS49l1uyJkIOL9kcTB3\n" +
                        "MK0hIYjgoF6eODZXkgHi8RZJhNlSdHJpnoRCZzVJa7uCOh2hAAs+jMD02MhzjcoS\n" +
                        "NPdEMncJyLB9qKNUUx1piGZfyazUWlzAQ2BRxJ4n7+VURCU+j84vBNwXZy5tkPTB\n" +
                        "5SqcJXjFiZh2MOvaHSgiH1c=\n" +
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
