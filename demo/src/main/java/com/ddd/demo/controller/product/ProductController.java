package com.ddd.demo.controller.product;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.common.response.PageResponse;
import com.ddd.demo.dto.product.ProductRequest;
import com.ddd.demo.dto.product.ProductResponse;
import com.ddd.demo.dto.product.ProductStatsResponse;
import com.ddd.demo.entity.product.Product;
import com.ddd.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    // Create product - Admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    // Get product by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    // Get product by SKU
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Get product details by SKU")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    // Update product
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Update product information")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    // Search products
    @GetMapping
    @Operation(summary = "Search products", description = "Search products with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(products)));
    }

    // Get products by category
    @GetMapping("/category/{category}")
    @Operation(summary = "Get by category", description = "Get products by category")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(products)));
    }

    // Get products by price range
    @GetMapping("/price-range")
    @Operation(summary = "Get by price range", description = "Get products within price range")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByPriceRange(
            @RequestParam @Min(0) BigDecimal minPrice,
            @RequestParam @Min(0) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "price") Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(products)));
    }

    // Delete product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Soft delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    // Update product status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update status", description = "Update product status")
    public ResponseEntity<ApiResponse<Void>> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Product.ProductStatus status) {
        productService.updateProductStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Product status updated"));
    }

    // Update stock
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update stock", description = "Update product stock quantity")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long id,
            @RequestParam @Min(0) Integer quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock updated successfully"));
    }

    // Get low stock products
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get low stock", description = "Get products with low stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") @Min(1) Integer threshold) {
        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    // Get product statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get product statistics by category")
    public ResponseEntity<ApiResponse<List<ProductStatsResponse>>> getProductStatistics() {
        List<ProductStatsResponse> stats = productService.getProductStatsByCategory();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}