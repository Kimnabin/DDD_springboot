package com.ddd.demo.dto.product;

import com.ddd.demo.common.validation.ValidSKU;
import com.ddd.demo.common.validation.ValidEnum;
import com.ddd.demo.entity.product.Product;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price must not exceed 9,999,999.99")
    @Digits(integer = 7, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    @Max(value = 999999, message = "Stock quantity must not exceed 999,999")
    private Integer stockQuantity;

    @ValidSKU(minLength = 3, maxLength = 50, message = "SKU must be 3-50 characters with uppercase letters, numbers and hyphens only")
    private String sku;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @ValidEnum(enumClass = Product.ProductStatus.class, message = "Invalid product status")
    private String status;
}