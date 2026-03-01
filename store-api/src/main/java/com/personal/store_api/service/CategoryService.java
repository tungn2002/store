package com.personal.store_api.service;

import com.personal.store_api.dto.request.CategoryRequest;
import com.personal.store_api.dto.response.CategoryResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.entity.Category;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.CategoryMapper;
import com.personal.store_api.repository.CategoryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    final CategoryRepository categoryRepository;
    final CategoryMapper categoryMapper;
    final CloudinaryService cloudinaryService;

    @Value("${cloudinary.folder:store-api}")
    String folder;

    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> getCategories(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        return PaginatedResponse.<CategoryResponse>builder()
                .items(categoryPage.getContent().stream()
                        .map(categoryMapper::toCategoryResponse)
                        .toList())
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalItems(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .isFirst(categoryPage.isFirst())
                .isLast(categoryPage.isLast())
                .hasNext(categoryPage.hasNext())
                .hasPrevious(categoryPage.hasPrevious())
                .build();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) throws IOException {
        Category category = Category.builder()
                .name(request.getName())
                .build();

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = uploadImage(request.getImage());
            category.setImage(imageUrl);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setName(request.getName());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            deleteOldImage(category.getImage());
            String imageUrl = uploadImage(request.getImage());
            category.setImage(imageUrl);
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Integer id) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        deleteOldImage(category.getImage());
        categoryRepository.delete(category);
    }

    private String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading category image: {}", file.getOriginalFilename());

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder + "/categories",
                "resource_type", "image"
        );

        Map uploadResult = cloudinaryService.uploadImage(file);
        return (String) uploadResult.get("secure_url");
    }

    private void deleteOldImage(String imageUrl) throws IOException {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                log.info("Deleting old category image: {}", publicId);
                try {
                    cloudinaryService.deleteImage(publicId);
                } catch (IOException e) {
                    log.warn("Failed to delete old image: {}. Continuing with update.", publicId);
                }
            }
        }
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String afterUpload = imageUrl.substring(uploadIndex + "/upload/".length());
        int lastSlashIndex = afterUpload.lastIndexOf('/');
        
        if (lastSlashIndex != -1 && lastSlashIndex < afterUpload.length() - 1) {
            String pathAndFilename = afterUpload.substring(lastSlashIndex + 1);
            int dotIndex = pathAndFilename.lastIndexOf('.');
            if (dotIndex != -1) {
                return pathAndFilename.substring(0, dotIndex);
            }
            return pathAndFilename;
        }

        return null;
    }
}
