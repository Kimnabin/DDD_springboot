package com.ddd.demo.dto.product;

import com.ddd.demo.entity.product.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private String category;
    private Product.ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}