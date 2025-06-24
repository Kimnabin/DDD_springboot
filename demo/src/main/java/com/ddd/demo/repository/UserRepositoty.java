package com.ddd.demo.repository;

import com.ddd.demo.dto.response.product.ProductResponseVO;
import com.ddd.demo.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// @RepositoryDefinition(domainClass = UserEntity.class, idClass = Long.class)
public interface UserRepositoty extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    // Method to find a user by username
    UserEntity findByUserName(String userName);

    // Method to find a user by username and password
    UserEntity findByUserNameAndPassword(String userName, String password);

    // Method to find a user by ID less than a specified value
    List<UserEntity> findByIdLessThan(Long id);

    // Method to find user with the maximum ID
    @Query("SELECT u FROM UserEntity u WHERE u.id = (SELECT MAX(p.id) FROM UserEntity p)")
    UserEntity findMaxIdUser();
}
