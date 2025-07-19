package com.ddd.demo.service.validation;

import com.ddd.demo.dto.order.OrderRequest;
import com.ddd.demo.entity.product.Product;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.ProductRepository;
import com.ddd.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessValidationService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<String> validateOrder(OrderRequest orderRequest, Long userId) {
        List<String> errors = new ArrayList<>();

        // Validate user exists and is active
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getIsActive()) {
            errors.add("User not found or inactive");
            return errors;
        }

        // Validate order items
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (var itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).orElse(null);

            if (product == null) {
                errors.add("Product not found: " + itemRequest.getProductId());
                continue;
            }

            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                errors.add("Product is not available: " + product.getProductName());
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                errors.add("Insufficient stock for product: " + product.getProductName() +
                        " (Available: " + product.getStockQuantity() +
                        ", Requested: " + itemRequest.getQuantity() + ")");
            }

            // Calculate subtotal
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            calculatedSubtotal = calculatedSubtotal.add(itemTotal);
        }

        // Validate subtotal matches
        if (orderRequest.getSubtotal() != null &&
                orderRequest.getSubtotal().compareTo(calculatedSubtotal) != 0) {
            errors.add("Subtotal mismatch. Expected: " + calculatedSubtotal +
                    ", Provided: " + orderRequest.getSubtotal());
        }

        return errors;
    }

    public List<String> validateUserRegistration(String username, String email) {
        List<String> errors = new ArrayList<>();

        if (userRepository.existsByUsername(username)) {
            errors.add("Username '" + username + "' already exists");
        }

        if (userRepository.existsByEmail(email)) {
            errors.add("Email '" + email + "' already exists");
        }

        return errors;
    }

    public List<String> validateProductCreation(String sku, String productName) {
        List<String> errors = new ArrayList<>();

        if (productRepository.findBySku(sku).isPresent()) {
            errors.add("SKU '" + sku + "' already exists");
        }

        // Additional business rules can be added here
        if (productName != null && productName.toLowerCase().contains("banned")) {
            errors.add("Product name contains prohibited words");
        }

        return errors;
    }
}