package com.ddd.demo.security;

import com.ddd.demo.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Enhanced JWT Authentication Filter with improved error handling and performance
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;

    // Endpoints that should skip JWT authentication
    private static final List<String> SKIP_FILTER_PATHS = Arrays.asList(
            "/api/v1/auth/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/actuator/health",
            "/actuator/info",
            "/error",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractTokenFromRequest(request);

            if (StringUtils.hasText(jwt) && shouldProcessToken(request)) {
                processJwtToken(jwt, request);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_FILTER_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private boolean shouldProcessToken(HttpServletRequest request) {
        // Skip processing if user is already authenticated
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void processJwtToken(String jwt, HttpServletRequest request) {
        if (!authService.validateToken(jwt)) {
            log.debug("Invalid JWT token");
            return;
        }

        Long userId = authService.getUserIdFromToken(jwt);

        if (userId == null) {
            log.debug("No user ID found in JWT token");
            return;
        }

        // Set user ID in request for controllers
        request.setAttribute("userId", userId);

        // Load user details and set authentication
        UserDetails userDetails = userDetailsService.loadUserById(userId);

        if (userDetails == null || !userDetails.isEnabled()) {
            log.debug("User not found or disabled: {}", userId);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Successfully authenticated user: {}", userDetails.getUsername());
    }
}