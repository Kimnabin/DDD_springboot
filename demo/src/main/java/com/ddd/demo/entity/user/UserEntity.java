package com.ddd.demo.entity.user;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Entity
@Table(name="java_user_001")
@DynamicInsert
@DynamicUpdate
public class UserEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_name", nullable = false, length = 50, columnDefinition = "varchar(255) comment 'user name'")
    private String userName;
    @Column(name = "user_password", nullable = false, length = 100, columnDefinition = "varchar(255) comment 'user password'")
    private String password;

}
