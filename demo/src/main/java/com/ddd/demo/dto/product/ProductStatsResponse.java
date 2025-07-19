package com.ddd.demo.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsResponse {
    private String category;
    private Long totalProducts;
    private BigDecimal averagePrice;
    private Long totalStock;
    private Long activeProducts;
    private Long inactiveProducts;
    private Long outOfStockProducts;
}