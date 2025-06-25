package com.ddd.demo.service.impl;

import com.ddd.demo.entity.Product;
import com.ddd.demo.repository.ProductRepositoty;
import com.ddd.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepositoty productRepositoty;

    @Override
    public Product createProduct(Product product) {
        return productRepositoty.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepositoty.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepositoty.findById(id)
                .orElse(null); // Return null if product not found
    }

    @Override
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepositoty.findAll(pageable);
    }

    @Override
    public Page<Product> findByProductNameContaining(String productName, Pageable pageable) {
        return productRepositoty.findByProductNameContaining(productName, pageable);
    }
}
