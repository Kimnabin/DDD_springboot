package com.ddd.demo.repository;

import com.ddd.demo.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// @RepositoryDefinition(domainClass = UserEntity.class, idClass = Long.class)
public interface UserRepositoty extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Method to find a user by username
    User findByUserName(String userName);

    // Method to find a user by username and password
    User findByUserNameAndPassword(String userName, String password);

    // Method to find a user by ID less than a specified value
    List<User> findByIdLessThan(Long id);

    // Method to find user with the maximum ID
    @Query("SELECT u FROM User u WHERE u.id = (SELECT MAX(p.id) FROM User p)")
    User findMaxIdUser();
}
