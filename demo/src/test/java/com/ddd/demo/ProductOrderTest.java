package com.ddd.demo;

import com.ddd.demo.entity.Order;
import com.ddd.demo.entity.Product;
import com.ddd.demo.repository.OrderRepositoty;
import com.ddd.demo.repository.ProductRepositoty;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class ProductOrderTest {

    @Autowired
    private ProductRepositoty productRepositoty;

    @Autowired
    private OrderRepositoty orderRepositoty;

    @Test
    @Transactional
    @Rollback(false)
    void manyToManyInsertTest() {
        Product p1 = new Product();
        Product p2 = new Product();

        Order o1 = new Order();
        Order o2 = new Order();
        Order o3 = new Order();

        p1.setProductName("Product 1");
        p1.setProductPrice(new BigDecimal(100));

        p2.setProductName("Product 2");
        p2.setProductPrice(new BigDecimal(200));

        o1.setUserId(1);
        o2.setUserId(2);

        // List order in product
        p1.setOrderList(List.of(o1, o2));
        p2.setOrderList(List.of(o1, o2, o3));

        orderRepositoty.save(o1);
        orderRepositoty.save(o2);
        orderRepositoty.save(o3);

        productRepositoty.save(p1);
        productRepositoty.save(p2);

    }
}
