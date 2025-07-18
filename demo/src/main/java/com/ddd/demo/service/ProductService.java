package com.ddd.demo.service;

import com.ddd.demo.dto.product.ProductRequest;
import com.ddd.demo.dto.product.ProductResponse;
import com.ddd.demo.dto.product.ProductStatsResponse;
import com.ddd.demo.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // Create new product
    ProductResponse createProduct(ProductRequest request);

    // Get product by ID
    ProductResponse getProductById(Long id);

    // Get product by SKU
    ProductResponse getProductBySku(String sku);

    // Update product
    ProductResponse updateProduct(Long id, ProductRequest request);

    // Search products
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    // Get products by category
    Page<ProductResponse> getProductsByCategory(String category, Pageable pageable);

    // Get products by price range
    Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Delete product (soft delete)
    void deleteProduct(Long id);

    // Update product status
    void updateProductStatus(Long id, Product.ProductStatus status);

    // Update stock quantity
    void updateStock(Long productId, Integer quantity);

    // Decrease stock (for orders)
    void decreaseStock(Long productId, Integer quantity);

    // Get low stock products
    List<ProductResponse> getLowStockProducts(Integer threshold);

    // Get product statistics by category
    List<ProductStatsResponse> getProductStatsByCategory();

    // Check if SKU exists
    boolean existsBySku(String sku);
}