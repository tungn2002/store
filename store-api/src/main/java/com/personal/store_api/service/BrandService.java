package com.personal.store_api.service;

import com.personal.store_api.dto.request.BrandRequest;
import com.personal.store_api.dto.response.BrandResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.entity.Brand;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.BrandMapper;
import com.personal.store_api.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    final BrandRepository brandRepository;
    final BrandMapper brandMapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<BrandResponse> getBrands(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        return PaginatedResponse.<BrandResponse>builder()
                .items(brandPage.getContent().stream()
                        .map(brandMapper::toBrandResponse)
                        .toList())
                .page(brandPage.getNumber())
                .size(brandPage.getSize())
                .totalItems(brandPage.getTotalElements())
                .totalPages(brandPage.getTotalPages())
                .isFirst(brandPage.isFirst())
                .isLast(brandPage.isLast())
                .hasNext(brandPage.hasNext())
                .hasPrevious(brandPage.hasPrevious())
                .build();
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand = Brand.builder()
                .name(request.getName())
                .build();

        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(savedBrand);
    }

    @Transactional
    public BrandResponse updateBrand(Integer id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        brand.setName(request.getName());

        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        brandRepository.delete(brand);
    }
}
