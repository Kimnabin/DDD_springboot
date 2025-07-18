package com.ddd.demo.entity.user;

import com.ddd.demo.entity.base.BaseEntity;
import com.ddd.demo.entity.feed.Feed;
import com.ddd.demo.entity.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    // Lazy loading để tối ưu performance
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Feed> feeds = new ArrayList<>();

    // One user can have many orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    public enum UserRole {
        USER, ADMIN, MODERATOR
    }
}