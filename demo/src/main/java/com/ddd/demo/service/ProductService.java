package com.ddd.demo.service;

import com.ddd.demo.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ProductService {

    /**
     * Creates a new product.
     *
     * @param product the product entity to create
     * @return the created product entity
     */
    ProductEntity   createProduct(ProductEntity product);

    /**
     * Retrieves all products.
     *
     * @return a list of all product entities
     */
    List<ProductEntity> getAllProducts();

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return the product entity with the specified ID, or null if not found
     */
    ProductEntity getProductById(Long id);

    /**
     * Retrieves all products with pagination.
     *
     * @param pageable the pagination information
     * @return a page of product entities
     */
    // get all products by limit and offset
    Page<ProductEntity> findAllProducts(Pageable pageable);

    /**
     * Searches for products by name containing a substring with pagination.
     *
     * @param productName the substring to search for in product names
     * @param pageable    the pagination information
     * @return a page of product entities matching the search criteria
     */
    Page<ProductEntity> findByProductNameContaining(String productName, Pageable pageable);

}
