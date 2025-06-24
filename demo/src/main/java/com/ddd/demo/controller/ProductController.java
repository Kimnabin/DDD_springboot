package com.ddd.demo.controller;

import com.ddd.demo.entity.ProductEntity;
import com.ddd.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.web.bind.annotation.*;

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

    /**
     * search product by name
     * @param page
     * @param size
     * @param sort
     * @param direction
     * @return Page<ProductEntity>
     */
    @GetMapping("/allProductsByPageable")
    public Page<ProductEntity> getAllProductsByPageable(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        return productService.findAllProducts(pageable);
    }

    /**
     * search product by name with pagination
     * @param productName
     * @param page
     * @param size
     * @param sort
     * @param direction
     * @return Page<ProductEntity>
     */
    @GetMapping("/searchPage")
    public Page<ProductEntity> getProductByPageable(
            @RequestParam String productName,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        return productService.findByProductNameContaining(productName, pageable);
    }
}
