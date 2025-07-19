package com.ddd.demo.controller.product;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.common.validation.ValidationGroups;
import com.ddd.demo.dto.product.ProductRequest;
import com.ddd.demo.dto.product.ProductResponse;
import com.ddd.demo.service.ProductService;
import com.ddd.demo.service.validation.BusinessValidationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
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
public class ProductController {

    private final ProductService productService;
    private final BusinessValidationService businessValidationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Validated(ValidationGroups.ProductCreate.class) @RequestBody ProductRequest request) {

        // Business validation
        List<String> businessErrors = businessValidationService.validateProductCreation(
                request.getSku(), request.getProductName());

        if (!businessErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed", String.join(", ", businessErrors)));
        }

        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Get products within price range")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByPriceRange(
            @RequestParam
            @DecimalMin(value = "0.01", message = "Minimum price must be greater than 0")
            @DecimalMax(value = "9999999.99", message = "Minimum price is too large")
            BigDecimal minPrice,

            @RequestParam
            @DecimalMin(value = "0.01", message = "Maximum price must be greater than 0")
            @DecimalMax(value = "9999999.99", message = "Maximum price is too large")
            BigDecimal maxPrice) {

        // Custom validation logic
        if (minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed", "Minimum price cannot be greater than maximum price"));
        }

        // Implementation continues...
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam
            @NotBlank(message = "Search keyword cannot be blank")
            @Size(min = 2, max = 100, message = "Search keyword must be between 2 and 100 characters")
            @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Search keyword contains invalid characters")
            String keyword) {

        // Implementation continues...
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}