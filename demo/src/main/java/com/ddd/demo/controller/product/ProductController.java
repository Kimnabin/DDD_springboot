package com.ddd.demo.controller.product;

import com.ddd.demo.entity.Product;
import com.ddd.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Product createProduct(Product product) {
        return productService.createProduct(product);
    }

    /**
     * list all products
     * @return List<ProductEntity>
     */
    @GetMapping("/searchAllProducts")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * get product by id
     * @param id
     * @return ProductEntity
     */
    @GetMapping("/searchById")
    public Product getProductById(Long id) {
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
    public Page<Product> getAllProductsByPageable(
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
    public Page<Product> getProductByPageable(
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
