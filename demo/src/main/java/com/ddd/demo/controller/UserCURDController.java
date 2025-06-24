package com.ddd.demo.controller;

import com.ddd.demo.entity.user.UserEntity;
import com.ddd.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserCURDController {

    @Autowired
    private UserService userService;

    public UserCURDController(UserService userService) {
        this.userService = userService;
    }

    // Add methods for user creation, retrieval, update, and deletion here

    // Create User
    @PostMapping("/add")
    public UserEntity addUser(@RequestBody UserEntity user) {
        return userService.createUser(user);
    }

    // Search user by ID
    @GetMapping("/searchById")
    public UserEntity findById(Long userId) {
        return userService.getUserById(userId);
    }

    // Search user by username
    @GetMapping("/searchByName")
    public UserEntity findByUserName(@RequestParam String userName) {
        return userService.getUserByName(userName);
    }

    // Search user by username and password
    @GetMapping("/searchByNameAndPassword")
    public UserEntity findByUserNameAndPassword(@RequestParam String userName, @RequestParam String password) {
        return userService.findByUserNameAndPassword(userName, password);
    }

    @GetMapping("/searchAllUsers")
    public List<UserEntity> findAllUsers() {
        return userService.getAllUsers();
    }

}
