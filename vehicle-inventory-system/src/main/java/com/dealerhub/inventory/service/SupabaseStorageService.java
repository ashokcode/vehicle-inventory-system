package com.dealerhub.inventory.service;

import com.dealerhub.inventory.config.SupabaseProperties;
import com.dealerhub.inventory.exception.PhotoStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Talks to Supabase Storage over its plain REST API rather than pulling in the
 * (Node-oriented) supabase-js client — this is the entire integration surface,
 * so a plain {@link HttpClient} keeps the dependency footprint down.
 *
 * <p>Requires the target bucket ({@link SupabaseProperties#getStorageBucket()})
 * to exist and to be set to public, since vehicle photos are shown directly in
 * the admin UI's <img> tags without a signed-URL round trip.
 */
@Service
public class SupabaseStorageService implements PhotoStorageService {

    private final SupabaseProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public SupabaseStorageService(SupabaseProperties properties) {
        this.properties = properties;
    }

    @Override
    public UploadResult upload(UUID vehicleId, MultipartFile file) {
        String storagePath = buildStoragePath(vehicleId, file);
        URI uploadUri = URI.create("%s/storage/v1/object/%s/%s".formatted(
                properties.getUrl(), properties.getStorageBucket(), encodePath(storagePath)));

        try {
            HttpRequest request = HttpRequest.newBuilder(uploadUri)
                    .header("Authorization", "Bearer " + properties.getServiceRoleKey())
                    .header("apikey", properties.getServiceRoleKey())
                    .header("Content-Type", resolveContentType(file))
                    .header("x-upsert", "false")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new PhotoStorageException(
                        "Supabase Storage upload failed (HTTP %d): %s".formatted(response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new PhotoStorageException("Could not reach Supabase Storage", e);
        }

        String publicUrl = "%s/storage/v1/object/public/%s/%s".formatted(
                properties.getUrl(), properties.getStorageBucket(), encodePath(storagePath));
        return new UploadResult(storagePath, publicUrl);
    }

    @Override
    public void delete(String storagePath) {
        URI deleteUri = URI.create("%s/storage/v1/object/%s/%s".formatted(
                properties.getUrl(), properties.getStorageBucket(), encodePath(storagePath)));
        try {
            HttpRequest request = HttpRequest.newBuilder(deleteUri)
                    .header("Authorization", "Bearer " + properties.getServiceRoleKey())
                    .header("apikey", properties.getServiceRoleKey())
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300 && response.statusCode() != 404) {
                throw new PhotoStorageException(
                        "Supabase Storage delete failed (HTTP %d): %s".formatted(response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new PhotoStorageException("Could not reach Supabase Storage", e);
        }
    }

    private String buildStoragePath(UUID vehicleId, MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "photo" : file.getOriginalFilename();
        String sanitized = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "vehicles/%s/%s-%s".formatted(vehicleId, UUID.randomUUID(), sanitized);
    }

    private String resolveContentType(MultipartFile file) {
        String type = file.getContentType();
        return type != null ? type : "application/octet-stream";
    }

    /** Encode each path segment separately so the '/' separators survive. */
    private String encodePath(String path) {
        String[] segments = path.split("/");
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                encoded.append("/");
            }
            encoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return encoded.toString();
    }
}
