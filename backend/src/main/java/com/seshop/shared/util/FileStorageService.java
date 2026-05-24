package com.seshop.shared.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileStorageService {

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/";
    private static final String S3_KEY_PREFIX = "product-media/";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final String bucket;
    private final Path localFallbackRoot = Paths.get("uploads");

    public FileStorageService(
            @Value("${seshop.storage.s3.bucket}") String bucket,
            @Value("${seshop.storage.s3.region}") String region,
            @Value("${seshop.storage.s3.endpoint:}") String endpoint,
            @Value("${seshop.storage.s3.access-key:}") String accessKey,
            @Value("${seshop.storage.s3.secret-key:}") String secretKey
    ) {
        this.bucket = bucket;

        S3ClientBuilderFactory builderFactory = new S3ClientBuilderFactory(region, endpoint, accessKey, secretKey);
        this.s3Client = builderFactory.create();
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        String key = S3_KEY_PREFIX + filename;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType(file.getContentType()))
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return PUBLIC_UPLOAD_PREFIX + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for S3 upload.", e);
        } catch (SdkClientException e) {
            return storeLocalFallback(file, key);
        } catch (S3Exception e) {
            return storeLocalFallback(file, key);
        }
    }

    public StoredFile load(String key) {
        String normalizedKey = normalizeKey(key);

        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(normalizedKey)
                    .build());
            GetObjectResponse response = object.response();
            return new StoredFile(object, contentType(response.contentType()), response.contentLength());
        } catch (NoSuchKeyException e) {
            return loadLocalFallback(normalizedKey)
                    .orElseThrow(() -> new NoSuchElementException("Uploaded file not found."));
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return loadLocalFallback(normalizedKey)
                        .orElseThrow(() -> new NoSuchElementException("Uploaded file not found."));
            }
            throw e;
        }
    }

    private Optional<StoredFile> loadLocalFallback(String key) {
        Path rootPath = localFallbackRoot.toAbsolutePath().normalize();
        Path filePath = localFallbackRoot.resolve(key).normalize().toAbsolutePath();
        if (!filePath.startsWith(rootPath) || !Files.isRegularFile(filePath)) {
            filePath = localFallbackRoot.resolve(Paths.get(key).getFileName()).normalize().toAbsolutePath();
        }
        
        if (!filePath.startsWith(rootPath) || !Files.isRegularFile(filePath)) {
            return Optional.empty();
        }

        try {
            InputStream inputStream = Files.newInputStream(filePath);
            String contentType = contentType(Files.probeContentType(filePath));
            return Optional.of(new StoredFile(inputStream, contentType, Files.size(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file.", e);
        }
    }

    private String storeLocalFallback(MultipartFile file, String key) {
        Path rootPath = localFallbackRoot.toAbsolutePath().normalize();
        Path filePath = localFallbackRoot.resolve(key).normalize().toAbsolutePath();

        if (!filePath.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid uploaded file path.");
        }

        try {
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath);
            return PUBLIC_UPLOAD_PREFIX + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file locally.", e);
        }
    }

    private String normalizeKey(String key) {
        if (!StringUtils.hasText(key) || key.contains("..")) {
            throw new IllegalArgumentException("Invalid uploaded file path.");
        }
        String normalized = key.startsWith(PUBLIC_UPLOAD_PREFIX)
                ? key.substring(PUBLIC_UPLOAD_PREFIX.length())
                : key;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!StringUtils.hasText(normalized) || normalized.contains("..")) {
            throw new IllegalArgumentException("Invalid uploaded file path.");
        }
        return normalized;
    }

    private String contentType(String value) {
        return StringUtils.hasText(value) ? value : DEFAULT_CONTENT_TYPE;
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1).replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    public record StoredFile(InputStream inputStream, String contentType, long contentLength) {
    }

    private static final class S3ClientBuilderFactory {
        private final String region;
        private final String endpoint;
        private final String accessKey;
        private final String secretKey;

        private S3ClientBuilderFactory(String region, String endpoint, String accessKey, String secretKey) {
            this.region = region;
            this.endpoint = endpoint;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        private S3Client create() {
            var builder = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider());

            if (StringUtils.hasText(endpoint)) {
                builder.endpointOverride(URI.create(endpoint))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build());
            }

            return builder.build();
        }

        private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider credentialsProvider() {
            if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
                return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            }
            return DefaultCredentialsProvider.create();
        }
    }
}
