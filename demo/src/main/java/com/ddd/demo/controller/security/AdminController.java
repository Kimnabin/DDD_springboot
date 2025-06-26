package com.ddd.demo.controller.security;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@RolesAllowed("ADMIN")
public class AdminController {

//    @RolesAllowed("ADMIN")
    @GetMapping("/vip")
    public String zoneVip() {
        return "Welcome to VIP zone";
    }

//    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping("/normal")
    public String zoneNormal() {
        return "Welcome to normal zone";
    }

    @GetMapping("/info")
    public Authentication getInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}