package com.ddd.demo.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @RequestMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("Login success");
    }

    @RequestMapping("/register")
    public ResponseEntity<String> register() {
        return ResponseEntity.ok("Register success");
    }
}