package com.personal.store_api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    final Cloudinary cloudinary;

    @Value("${cloudinary.folder:store-api}")
    String folder;

    public Map uploadFile(MultipartFile file) throws IOException {
        log.info("Uploading file: {} to Cloudinary", file.getOriginalFilename());

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto"
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    public Map uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image: {} to Cloudinary", file.getOriginalFilename());

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
        );

        return cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    public void deleteFile(String fileId) throws IOException {
        log.info("Deleting file: {} from Cloudinary", fileId);

        Map deleteResult = cloudinary.uploader().destroy(fileId, ObjectUtils.emptyMap());

        if (!"ok".equals(deleteResult.get("result"))) {
            log.error("Failed to delete file: {}. Result: {}", fileId, deleteResult.get("result"));
            throw new IOException("Failed to delete file: " + fileId);
        }

        log.info("Successfully deleted file: {}", fileId);
    }

    public void deleteImage(String publicId) throws IOException {
        log.info("Deleting image: {} from Cloudinary", publicId);

        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));

        if (!"ok".equals(deleteResult.get("result"))) {
            log.error("Failed to delete image: {}. Result: {}", publicId, deleteResult.get("result"));
            throw new IOException("Failed to delete image: " + publicId);
        }

        log.info("Successfully deleted image: {}", publicId);
    }
}
