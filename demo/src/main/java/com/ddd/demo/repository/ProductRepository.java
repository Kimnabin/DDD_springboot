package com.ddd.demo.repository;

import com.ddd.demo.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by SKU for unique product identification
    Optional<Product> findBySku(String sku);

    // Search products with pagination
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    // Find products by category with active status
    Page<Product> findByCategoryAndStatus(String category, Product.ProductStatus status, Pageable pageable);

    // Find products in price range
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.status = 'ACTIVE'")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    // Update stock quantity
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Find low stock products
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Get product statistics by category
    @Query("SELECT p.category, COUNT(p), AVG(p.price) FROM Product p GROUP BY p.category")
    List<Object[]> getProductStatsByCategory();
}