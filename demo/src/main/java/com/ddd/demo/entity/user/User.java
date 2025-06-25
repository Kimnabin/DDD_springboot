package com.ddd.demo.entity.user;

import com.ddd.demo.entity.feed.Feed;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Data
@Entity
@Table(name="java_user_001")
@DynamicInsert
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_name", nullable = false, length = 50, columnDefinition = "varchar(255) comment 'user name'")
    private String userName;
    @Column(name = "user_password", nullable = false, length = 100, columnDefinition = "varchar(255) comment 'user password'")
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feed> feedList; // One-to-many relationship with Feed entity
}
