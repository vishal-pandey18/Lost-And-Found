// package com.lostfound.service;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.StandardCopyOption;
// import java.util.List;
// import java.util.UUID;

// // Stores uploaded images on local disk under app.upload.dir, served via
// // WebConfig's /uploads/** handler. Intentionally simple so this works with
// // no cloud credentials; swap in an S3 implementation later.
// @Service
// public class LocalFileStorageService implements FileStorageService {

//     private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

//     private final Path uploadDir;
//     private final String baseUrl;

//     public LocalFileStorageService(
//             @Value("${app.upload.dir}") String uploadDir,
//             @Value("${app.upload.base-url}") String baseUrl
//     ) {
//         this.uploadDir = Path.of(uploadDir);
//         this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
//         try {
//             Files.createDirectories(this.uploadDir);
//         } catch (IOException e) {
//             throw new IllegalStateException("Could not create upload directory: " + uploadDir, e);
//         }
//     }

//     @Override
//     public String store(MultipartFile file) {
//         if (file == null || file.isEmpty()) {
//             throw new IllegalArgumentException("Cannot upload an empty file");
//         }

//         String originalFilename = StringUtils.cleanPath(
//                 file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

//         String extension = "";
//         int dotIndex = originalFilename.lastIndexOf('.');
//         if (dotIndex >= 0) {
//             extension = originalFilename.substring(dotIndex).toLowerCase();
//         }

//         if (!ALLOWED_EXTENSIONS.contains(extension)) {
//             throw new IllegalArgumentException(
//                     "Only image files are allowed (" + String.join(", ", ALLOWED_EXTENSIONS) + ")");
//         }

//         String storedFilename = UUID.randomUUID() + extension;
//         Path targetPath = uploadDir.resolve(storedFilename);

//         try (InputStream in = file.getInputStream()) {
//             Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
//         } catch (IOException e) {
//             throw new IllegalStateException("Failed to store file " + originalFilename, e);
//         }

//         return baseUrl + "/" + storedFilename;
//     }
// }



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
public class LocalFileStorageService implements FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Cloudinary cloudinary;

    public LocalFileStorageService(
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
