package com.ddd.demo.service.impl;

import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.UserRepositoty;
import com.ddd.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepositoty userRepositoty;

    @Override
    public User createUser(User user) {
        return userRepositoty.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepositoty.findById(id)
                .orElse(null); // Return null if user not found
    }

    @Override
    public User getUserByName(String userName) {
        return userRepositoty.findByUserName(userName);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepositoty.findAll();
    }

    @Override
    public User findByUserNameAndPassword(String userName, String password) {
        return userRepositoty.findByUserNameAndPassword(userName, password);
    }

    @Override
    public List<User> findByIdLessThan(Long id) {
        return userRepositoty.findByIdLessThan(id);
    }
}
