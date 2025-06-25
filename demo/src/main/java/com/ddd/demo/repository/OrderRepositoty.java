package com.ddd.demo.repository;

import com.ddd.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepositoty extends JpaRepository<Order,  Long> {
}
