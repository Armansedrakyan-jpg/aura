package com.example.auraclone.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("classpath:service-account.json")
    private Resource serviceAccountFile;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Value("${firebase.database-url:https://aura-24ae8-default-rtdb.europe-west1.firebasedatabase.app/}")
    private String databaseUrl;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        if (!firebaseEnabled) {
            log.info("Firebase initialization skipped (firebase.enabled=false)");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = resolveCredentialsStream()) {
            if (serviceAccount == null) {
                log.warn("Firebase credentials not found; battle endpoints will return 503 until configured");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }

    private InputStream resolveCredentialsStream() throws IOException {
        String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS_JSON");
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            Path path = Path.of(credentialsPath);
            if (Files.exists(path)) {
                log.info("Loading Firebase credentials from path: {}", path);
                return Files.newInputStream(path);
            }
        }

        if (serviceAccountFile != null && serviceAccountFile.exists()) {
            log.info("Loading Firebase credentials from classpath:service-account.json");
            return serviceAccountFile.getInputStream();
        }

        return null;
    }
}

