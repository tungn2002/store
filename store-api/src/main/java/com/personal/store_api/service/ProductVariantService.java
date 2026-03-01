package com.personal.store_api.service;

import com.personal.store_api.dto.request.ProductVariantRequest;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.entity.Product;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.ProductVariantMapper;
import com.personal.store_api.repository.ProductRepository;
import com.personal.store_api.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {

    final ProductVariantRepository productVariantRepository;
    final ProductVariantMapper productVariantMapper;
    final ProductRepository productRepository;
    final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductVariantResponse> getProductVariants(Integer productId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ProductVariant> variantPage = productVariantRepository.findByProductId(productId, pageable);

        return PaginatedResponse.<ProductVariantResponse>builder()
                .items(variantPage.getContent().stream()
                        .map(productVariantMapper::toProductVariantResponse)
                        .toList())
                .page(variantPage.getNumber())
                .size(variantPage.getSize())
                .totalItems(variantPage.getTotalElements())
                .totalPages(variantPage.getTotalPages())
                .isFirst(variantPage.isFirst())
                .isLast(variantPage.isLast())
                .hasNext(variantPage.hasNext())
                .hasPrevious(variantPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getProductVariant(Integer id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        
        return productVariantMapper.toProductVariantResponse(variant);
    }

    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest request) throws IOException {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Check for duplicate size+color combination
        Optional<ProductVariant> existingVariant = productVariantRepository.findByProductIdAndSizeAndColor(
                request.getProductId(), 
                request.getSize(), 
                request.getColor()
        );

        if (existingVariant.isPresent()) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_DUPLICATE);
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(request.getSize())
                .color(request.getColor())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = uploadImage(request.getImage());
            variant.setImage(imageUrl);
        }

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return productVariantMapper.toProductVariantResponse(savedVariant);
    }

    @Transactional
    public ProductVariantResponse updateProductVariant(Integer id, ProductVariantRequest request) throws IOException {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Check for duplicate size+color combination (excluding current variant)
        Optional<ProductVariant> existingVariant = productVariantRepository.findByProductIdAndSizeAndColor(
                request.getProductId(), 
                request.getSize(), 
                request.getColor()
        );

        if (existingVariant.isPresent() && !existingVariant.get().getId().equals(id)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_DUPLICATE);
        }

        variant.setProduct(product);
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            deleteOldImage(variant.getImage());
            String imageUrl = uploadImage(request.getImage());
            variant.setImage(imageUrl);
        }

        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return productVariantMapper.toProductVariantResponse(updatedVariant);
    }

    @Transactional
    public void deleteProductVariant(Integer id) throws IOException {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        deleteOldImage(variant.getImage());
        productVariantRepository.delete(variant);
    }

    private String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading product variant image: {}", file.getOriginalFilename());

        Map<String, Object> uploadParams = Map.of(
                "folder", "store-api/product-variants",
                "resource_type", "image"
        );

        Map uploadResult = cloudinaryService.uploadImage(file);
        return (String) uploadResult.get("secure_url");
    }

    private void deleteOldImage(String imageUrl) throws IOException {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                log.info("Deleting old product variant image: {}", publicId);
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
