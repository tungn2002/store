package com.personal.store_api.service;

import com.personal.store_api.dto.request.ProductRequest;
import com.personal.store_api.dto.response.NewProductResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductDetailResponse;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.entity.Category;
import com.personal.store_api.entity.Brand;
import com.personal.store_api.entity.Product;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.integration.media.CloudinaryService;
import com.personal.store_api.integration.search.ElasticsearchService;
import com.personal.store_api.mapper.ProductMapper;
import com.personal.store_api.mapper.CategoryMapper;
import com.personal.store_api.mapper.BrandMapper;
import com.personal.store_api.repository.ProductRepository;
import com.personal.store_api.repository.CategoryRepository;
import com.personal.store_api.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
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
    final CategoryMapper categoryMapper;
    final BrandMapper brandMapper;
    final ElasticsearchService elasticsearchService;

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#page + '-' + #size + '-' + #sortBy + '-' + #name + '-' + #categoryId + '-' + #brandId")
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
    @Cacheable(value = "products", key = "'product-' + #id")
    public ProductResponse getProduct(Integer id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'latest-5'")
    public List<NewProductResponse> getLatest5Products() {
        List<Product> products = productRepository.findTop5ByOrderByCreatedAtDesc();

        return products.stream()
                .map(product -> {
                    BigDecimal price = null;
                    if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                        price = product.getVariants().get(0).getPrice();
                    }
                    return NewProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .image(product.getImage())
                            .price(price)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'detail-' + #id")
    public ProductDetailResponse getProductDetail(Integer id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Collect all variant images (excluding null/empty)
        List<String> variantImages = product.getVariants().stream()
                .map(ProductVariant::getImage)
                .filter(img -> img != null && !img.isEmpty())
                .distinct()
                .toList();

        // Get price from first available variant
        BigDecimal price = product.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .findFirst()
                .map(ProductVariant::getPrice)
                .orElse(null);

        // Collect all unique colors
        List<String> colors = product.getVariants().stream()
                .map(ProductVariant::getColor)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .toList();

        // Collect all unique sizes
        List<String> sizes = product.getVariants().stream()
                .map(ProductVariant::getSize)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .toList();

        // Collect all variant prices with stock info
        List<ProductDetailResponse.VariantPriceStock> prices = product.getVariants().stream()
                .map(variant -> ProductDetailResponse.VariantPriceStock.builder()
                        .productVariantId(variant.getId())
                        .color(variant.getColor())
                        .size(variant.getSize())
                        .price(variant.getPrice())
                        .stock(variant.getStockQuantity())
                        .build())
                .toList();

        // Calculate total stock
        Integer totalStock = product.getVariants().stream()
                .mapToInt(variant -> variant.getStockQuantity() != null ? variant.getStockQuantity() : 0)
                .sum();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(categoryMapper.toCategoryResponse(product.getCategory()))
                .brand(brandMapper.toBrandResponse(product.getBrand()))
                .image(product.getImage())
                .variantImages(variantImages)
                .price(price)
                .colors(colors)
                .sizes(sizes)
                .prices(prices)
                .totalStock(totalStock)
                .build();
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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
        
        // Index to Elasticsearch
        elasticsearchService.indexProduct(savedProduct);
        
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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
        
        // Update in Elasticsearch
        elasticsearchService.updateProduct(updatedProduct);
        
        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Integer id) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        deleteOldImage(product.getImage());
        
        // Delete from Elasticsearch
        elasticsearchService.deleteProduct(id);
        
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
