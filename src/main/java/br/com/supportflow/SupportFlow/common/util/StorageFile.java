package br.com.supportflow.SupportFlow.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;

@Service
public class StorageFile {
    private final HttpClient client;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.serviceKey}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public StorageFile(HttpClient client) {
        this.client = client;
    }

    public String uploadFile(String base64, String folder, String contentType) {
        byte[] bytes = decodeBase64(base64);

        String safeFolder = (folder == null || folder.isBlank()) ? "" : (folder.endsWith("/") ? folder : folder + "/");
        String finalName = UUID.randomUUID() + guessExt(contentType);

        String path = safeFolder + finalName;

        URI uri = URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path);

        var request = HttpRequest.newBuilder(uri)
                .header("Authorization", "Bearer " + serviceKey)
                .header("Content-Type", (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType)
                .header("Cache-Control", "max-age=3600")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Supabase upload failed: " + response.statusCode() + " - " + response.body());
            }
            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        }catch (Exception ex) {
            throw new RuntimeException("Failed to upload file: " + ex.getMessage());
        }
    }

    public byte[] decodeBase64(String base64) {
        if(base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("Base64 string cannot be null or blank");
        }
        String cleaned = base64.replaceFirst("^data:.*;base64,", "");
        return Base64.getDecoder().decode(cleaned);
    }

    private String guessExt(String contentType) {
        if (contentType == null) return "";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

}
