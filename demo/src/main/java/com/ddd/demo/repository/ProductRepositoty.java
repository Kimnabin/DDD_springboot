package com.ddd.demo.repository;

import com.ddd.demo.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepositoty extends JpaRepository<ProductEntity, Long> {

}
