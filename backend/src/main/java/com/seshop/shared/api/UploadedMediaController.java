package com.seshop.shared.api;

import com.seshop.shared.util.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.NoSuchElementException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class UploadedMediaController {

    private static final String UPLOADS_PATH = "/uploads/";

    private final FileStorageService fileStorageService;

    public UploadedMediaController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<InputStreamResource> getUploadedMedia(HttpServletRequest request) {
        try {
            FileStorageService.StoredFile file = fileStorageService.load(extractKey(request));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    .contentLength(file.contentLength())
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .body(new InputStreamResource(file.inputStream()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid uploaded media path.", e);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(NOT_FOUND, "Uploaded media not found.", e);
        }
    }

    private String extractKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (!path.startsWith(UPLOADS_PATH)) {
            throw new IllegalArgumentException("Invalid uploads path.");
        }
        return URLDecoder.decode(path.substring(UPLOADS_PATH.length()), StandardCharsets.UTF_8);
    }
}
