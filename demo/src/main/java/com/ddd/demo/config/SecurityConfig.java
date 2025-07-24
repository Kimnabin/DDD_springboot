package com.ddd.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép file tĩnh
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/static/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ✅ Cho phép Chrome DevTools & trang lỗi hoạt động
                        .requestMatchers("/.well-known/**", "/error", "/error/**").permitAll()

                        // ✅ Cho phép các trang public (home, about…)
                        .requestMatchers("/", "/home", "/about", "/contact", "/login", "/sample/**").permitAll()

                        // ✅ Các request khác yêu cầu login
                        .anyRequest().authenticated()
                )

                // ✅ Sử dụng form login mặc định của Spring Security
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)

                // ✅ Cho phép logout mà không cần auth
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    /**
     * ✅ Tạo user mẫu để test
     *  - admin / admin  → ROLE_ADMIN + ROLE_USER
     *  - user  / user   → ROLE_USER
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "USER")
                .build();

        UserDetails user = User.withUsername("user")
                .password(passwordEncoder().encode("user"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
