package com.ddd.demo.service.validation;

import com.ddd.demo.repository.UserRepository;
import com.ddd.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseValidationService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Cacheable(value = "usernameExists", key = "#username")
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Cacheable(value = "emailExists", key = "#email")
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Cacheable(value = "skuExists", key = "#sku")
    public boolean skuExists(String sku) {
        return productRepository.findBySku(sku).isPresent();
    }

    public boolean isValidUsernameForUpdate(String username, Long userId) {
        return userRepository.findByUsername(username)
                .map(user -> user.getId().equals(userId))
                .orElse(true);
    }

    public boolean isValidEmailForUpdate(String email, Long userId) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(true);
    }

    public boolean isValidSkuForUpdate(String sku, Long productId) {
        return productRepository.findBySku(sku)
                .map(product -> product.getId().equals(productId))
                .orElse(true);
    }
}