package com.ddd.demo.repository;

import com.ddd.demo.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepositoty extends JpaRepository<ProductEntity, Long> {

    Page<ProductEntity> findByProductNameContaining(String productName, Pageable pageable);

}
