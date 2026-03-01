package com.personal.store_api.service;

import com.personal.store_api.dto.request.ProductRequest;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.entity.Category;
import com.personal.store_api.entity.Brand;
import com.personal.store_api.entity.Product;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.ProductMapper;
import com.personal.store_api.repository.ProductRepository;
import com.personal.store_api.repository.CategoryRepository;
import com.personal.store_api.repository.BrandRepository;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    final ProductRepository productRepository;
    final ProductMapper productMapper;
    final CategoryRepository categoryRepository;
    final BrandRepository brandRepository;
    final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getProducts(int page, int size, String sortBy, 
                                                           String name, Integer categoryId, Integer brandId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        
        Page<Product> productPage;
        
        if (name != null && !name.isEmpty() && categoryId != null && brandId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndBrandId(name, categoryId, brandId, pageable);
        } else if (name != null && !name.isEmpty() && categoryId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(name, categoryId, pageable);
        } else if (name != null && !name.isEmpty() && brandId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndBrandId(name, brandId, pageable);
        } else if (categoryId != null && brandId != null) {
            productPage = productRepository.findByCategoryIdAndBrandId(categoryId, brandId, pageable);
        } else if (name != null && !name.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (brandId != null) {
            productPage = productRepository.findByBrandId(brandId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return PaginatedResponse.<ProductResponse>builder()
                .items(productPage.getContent().stream()
                        .map(productMapper::toProductResponse)
                        .toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalItems(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isFirst(productPage.isFirst())
                .isLast(productPage.isLast())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Integer id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) throws IOException {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .brand(brand)
                .build();

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = uploadImage(request.getImage());
            product.setImage(imageUrl);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        product.setCategory(category);
        product.setBrand(brand);
        product.setName(request.getName());
        product.setDescription(request.getDescription());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            deleteOldImage(product.getImage());
            String imageUrl = uploadImage(request.getImage());
            product.setImage(imageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Integer id) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        deleteOldImage(product.getImage());
        productRepository.delete(product);
    }

    private String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading product image: {}", file.getOriginalFilename());

        Map<String, Object> uploadParams = Map.of(
                "folder", "store-api/products",
                "resource_type", "image"
        );

        Map uploadResult = cloudinaryService.uploadImage(file);
        return (String) uploadResult.get("secure_url");
    }

    private void deleteOldImage(String imageUrl) throws IOException {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                log.info("Deleting old product image: {}", publicId);
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
