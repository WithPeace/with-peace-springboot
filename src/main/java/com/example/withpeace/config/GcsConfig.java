package com.example.withpeace.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcsConfig {
    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String gcpCredentialsLocation;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials;
        if (gcpCredentialsLocation.startsWith("classpath:")) {
            Resource resource = new ClassPathResource(gcpCredentialsLocation.substring(10));
            InputStream inputStream = resource.getInputStream();
            credentials = GoogleCredentials.fromStream(inputStream);
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}