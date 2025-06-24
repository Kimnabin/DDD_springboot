package com.ddd.demo.service.impl;

import com.ddd.demo.entity.user.UserEntity;
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
    public UserEntity createUser(UserEntity user) {
        return userRepositoty.save(user);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepositoty.findById(id)
                .orElse(null); // Return null if user not found
    }

    @Override
    public UserEntity getUserByName(String userName) {
        return userRepositoty.findByUserName(userName);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepositoty.findAll();
    }

    @Override
    public UserEntity findByUserNameAndPassword(String userName, String password) {
        return userRepositoty.findByUserNameAndPassword(userName, password);
    }

    @Override
    public List<UserEntity> findByIdLessThan(Long id) {
        return userRepositoty.findByIdLessThan(id);
    }
}
