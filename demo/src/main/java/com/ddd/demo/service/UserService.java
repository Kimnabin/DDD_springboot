package com.ddd.demo.service;

import com.ddd.demo.entity.user.User;

import java.util.List;


public interface UserService {

    /**
     * Creates a new user.
     *
     * @param user the user entity to create
     * @return the created user entity
     */
    User createUser(User user);

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return the user entity with the specified ID, or null if not found
     */
    User getUserById(Long id);

    /**
     * Retrieves a user by their username.
     *
     * @param userName the username of the user to retrieve
     * @return the user entity with the specified username, or null if not found
     */
    User getUserByName(String userName);

    /**
     * Retrieves all users.
     *
     * @return a list of all user entities
     */
    List<User> getAllUsers();


    /**
     * Finds a user by username and password.
     * @param userName
     * @param password
     * @return
     */
    User findByUserNameAndPassword(String userName, String password);

    /**
     * Finds users with IDs less than the specified value.
     *
     * @param id the ID threshold
     * @return a list of user entities with IDs less than the specified value
     */
    List<User> findByIdLessThan(Long id);
}
