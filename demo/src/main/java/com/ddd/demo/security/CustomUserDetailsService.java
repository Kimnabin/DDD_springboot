package com.ddd.demo.security;

import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        User user = findUserByUsernameOrEmail(username);

        if (user == null || !user.getIsActive()) {
            log.warn("User not found or inactive: {}", username);
            throw new UsernameNotFoundException("User not found or inactive: " + username);
        }

        return createUserDetails(user);
    }

    /**
     * Load user by ID (used for JWT token validation)
     */
    @Cacheable(value = "userDetails", key = "#userId")
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user details for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!user.getIsActive()) {
            log.warn("User inactive: {}", userId);
            throw new UsernameNotFoundException("User inactive: " + userId);
        }

        return createUserDetails(user);
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by username first
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElse(null);
    }

    private UserDetails createUserDetails(User user) {
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        return CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .authorities(authorities)
                .enabled(user.getIsActive())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleName = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}