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

    // Spring сам найдет файл в папке ресурсов
    @Value("classpath:service-account.json")
    private Resource serviceAccountFile;

    @PostConstruct
    public void init() {
        try {
            if (!serviceAccountFile.exists()) {
                System.err.println("🔴 ОШИБКА: Файл service-account.json не найден в папке ресурсов!");
                return;
            }

            InputStream serviceAccount = serviceAccountFile.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://aura-24ae8-default-rtdb.europe-west1.firebasedatabase.app/")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("🟢 Firebase успешно подключен к проекту aura!");
            }
        } catch (Exception e) {
            System.err.println("🔴 Ошибка при инициализации Firebase:");
            e.printStackTrace();
        }
    }
}
