package com.ddd.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Activate Spring Security
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // backend and frontend are separated, so we need to disable CSRF
        http.formLogin((formLogin) -> formLogin.loginProcessingUrl("/login"));
        http.authorizeHttpRequests(req -> req
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/home/**") // Public endpoints
                .permitAll()                    // Allow public access to login and register endpoints
                .anyRequest().authenticated()); // All other requests require authentication
        return http.build();
    }

    /**
     * Config user information
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder().encode("admin"))
//                .roles("admin", "user")
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();

        UserDetails user = User
                .withUsername("user")
                .password(passwordEncoder().encode("user"))
//                .roles("user")
                .authorities("ROLE_USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}