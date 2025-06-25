package com.ddd.demo.controller.user;

import com.ddd.demo.entity.user.User;
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
    public User addUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Search user by ID
    @GetMapping("/searchById")
    public User findById(Long userId) {
        return userService.getUserById(userId);
    }

    // Search user by username
    @GetMapping("/searchByName")
    public User findByUserName(@RequestParam String userName) {
        return userService.getUserByName(userName);
    }

    // Search user by username and password
    @GetMapping("/searchByNameAndPassword")
    public User findByUserNameAndPassword(@RequestParam String userName, @RequestParam String password) {
        return userService.findByUserNameAndPassword(userName, password);
    }

    @GetMapping("/searchAllUsers")
    public List<User> findAllUsers() {
        return userService.getAllUsers();
    }

}
