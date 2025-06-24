package com.ddd.demo.service;

import com.ddd.demo.entity.ProductEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {

    ProductEntity   createProduct(ProductEntity product);

    List<ProductEntity> getAllProducts();

    ProductEntity getProductById(Long id);
}
