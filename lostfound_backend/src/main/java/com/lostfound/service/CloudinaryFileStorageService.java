package com.lostfound.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
public class CloudinaryFileStorageService implements FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(
            @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName,
            @Value("${CLOUDINARY_API_KEY}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String filename = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename();

        int dotIndex = filename.lastIndexOf('.');
        String extension = dotIndex >= 0
                ? filename.substring(dotIndex).toLowerCase()
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image extension");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", "lostfound/" + UUID.randomUUID(),
                            "resource_type", "image"
                    )
            );

            return result.get("secure_url").toString();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload image to Cloudinary", e);
        }
    }
}
