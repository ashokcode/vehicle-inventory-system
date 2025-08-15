package com.dealerhub.inventory.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PhotoStorageService {

    /** Uploads a file and returns its storage path and public URL. */
    UploadResult upload(UUID vehicleId, MultipartFile file);

    /** Permanently deletes a previously-uploaded file. */
    void delete(String storagePath);

    record UploadResult(String storagePath, String publicUrl) {
    }
}
