package com.ddd.demo.service.impl;

import com.ddd.demo.common.exception.BusinessException;
import com.ddd.demo.common.exception.ResourceNotFoundException;
import com.ddd.demo.dto.product.ProductRequest;
import com.ddd.demo.dto.product.ProductResponse;
import com.ddd.demo.dto.product.ProductStatsResponse;
import com.ddd.demo.entity.product.Product;
import com.ddd.demo.repository.ProductRepository;
import com.ddd.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        // Generate unique SKU if not provided
        String sku = request.getSku() != null ? request.getSku() : generateSku();

        // Validate SKU uniqueness
        if (existsBySku(sku)) {
            throw new BusinessException("Product with SKU " + sku + " already exists");
        }

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(sku)
                .category(request.getCategory())
                .status(Product.ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created new product: {} with SKU: {}", savedProduct.getProductName(), savedProduct.getSku());

        return mapToResponse(savedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update fields
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Updated product: {}", updatedProduct.getProductName());

        return mapToResponse(updatedProduct);
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryAndStatus(category, Product.ProductStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("Min price must be less than max price");
        }
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(Product.ProductStatus.INACTIVE);
        product.setIsDeleted(true);
        productRepository.save(product);

        log.info("Soft deleted product: {}", product.getProductName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void updateProductStatus(Long id, Product.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(status);
        productRepository.save(product);

        log.info("Updated product {} status to: {}", product.getProductName(), status);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStockQuantity(quantity);

        // Update status based on stock
        if (quantity == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }

        productRepository.save(product);
        log.info("Updated stock for product {} to: {}", product.getProductName(), quantity);
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        int updatedRows = productRepository.decreaseStock(productId, quantity);
        if (updatedRows == 0) {
            throw new BusinessException("Insufficient stock or product not found");
        }

        // Check and update status if out of stock
        Product product = productRepository.findById(productId).orElseThrow();
        if (product.getStockQuantity() == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            productRepository.save(product);
        }

        log.info("Decreased stock for product {} by: {}", productId, quantity);
    }

    @Override
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductStatsResponse> getProductStatsByCategory() {
        return productRepository.getProductStatsByCategory().stream()
                .map(obj -> ProductStatsResponse.builder()
                        .category((String) obj[0])
                        .totalProducts((Long) obj[1])
                        .averagePrice(BigDecimal.valueOf((Double) obj[2]))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.findBySku(sku).isPresent();
    }

    // Generate unique SKU
    private String generateSku() {
        return "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Helper method to map entity to response
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}