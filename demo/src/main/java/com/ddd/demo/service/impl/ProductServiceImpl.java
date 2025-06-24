package com.ddd.demo.service.impl;

import com.ddd.demo.entity.ProductEntity;
import com.ddd.demo.repository.ProductRepositoty;
import com.ddd.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepositoty productRepositoty;

    @Override
    public ProductEntity createProduct(ProductEntity product) {
        return productRepositoty.save(product);
    }

    @Override
    public List<ProductEntity> getAllProducts() {
        return productRepositoty.findAll();
    }

    @Override
    public ProductEntity getProductById(Long id) {
        return productRepositoty.findById(id)
                .orElse(null); // Return null if product not found
    }
}
