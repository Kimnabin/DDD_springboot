package com.ddd.demo.controller;

import com.ddd.demo.entity.ProductEntity;
import com.ddd.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
// localhost:8080/v1/api/products
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * create a new product
     * @param product
     * @return
     */
    @PostMapping("/add")
    public ProductEntity createProduct(ProductEntity product) {
        return productService.createProduct(product);
    }

    /**
     * list all products
     * @return List<ProductEntity>
     */
    @GetMapping("/searchAllProducts")
    public List<ProductEntity> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * get product by id
     * @param id
     * @return ProductEntity
     */
    @GetMapping("/searchById")
    public ProductEntity getProductById(Long id) {
        return productService.getProductById(id);
    }
}
