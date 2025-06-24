package com.ddd.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Data
@Entity
@Table(name="java_product_001")
@DynamicInsert
@DynamicUpdate
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_name", nullable = false, length = 50, columnDefinition = "varchar(255) comment 'product name'")
    private String productName;
    @Column(name = "product_price", nullable = false, columnDefinition = "decimal(10,2) comment 'product price'")
    private BigDecimal productPrice;

}
