package com.example.auraclone.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("classpath:service-account.json")
    private Resource serviceAccountFile;

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;
            String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                // На Railway читаем из безопасной переменной окружения
                serviceAccount = new java.io.ByteArrayInputStream(
                        credentialsJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
                System.out.println("🟢 Firebase запущен через переменную (Railway)");
            } else if (serviceAccountFile.exists()) {
                // На твоем ПК читаем локальный файл
                serviceAccount = serviceAccountFile.getInputStream();
                System.out.println("🟢 Firebase запущен локально (IntelliJ)");
            } else {
                System.err.println("🔴 ОШИБКА: Сертификат Firebase не найден!");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://aura-24ae8-default-rtdb.europe-west1.firebasedatabase.app/")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
