package com.lostfound.service;

import org.springframework.web.multipart.MultipartFile;

// Abstraction so a LocalFileStorageService can be swapped for an
// S3/Cloudinary implementation without touching any calling code.
public interface FileStorageService {
    String store(MultipartFile file);
}
