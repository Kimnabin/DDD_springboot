package com.ddd.demo.config;

import com.ddd.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Enhanced Security Configuration with improved security practices
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${app.cors.exposed-headers}")
    private List<String> exposedHeaders;

    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
            // Authentication endpoints
            "/api/v1/auth/**",
            "/home/**",

            // Public product endpoints
            "/api/v1/products",
            "/api/v1/products/{id}",
            "/api/v1/products/sku/{sku}",
            "/api/v1/products/category/{category}",
            "/api/v1/products/price-range",

            // Documentation endpoints
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",

            // Monitoring endpoints
            "/actuator/health",
            "/actuator/info",

            // Static resources
            "/error",
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico"
    };

    // Admin-only endpoints
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/admin/**",
            "/v1/admin/**",
            "/actuator/**"
    };

    // User-specific endpoints
    private static final String[] USER_ENDPOINTS = {
            "/api/v1/users/me",
            "/api/v1/orders/my-orders"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure security headers
                .headers(headers -> {
                    headers
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                            .maxAgeInSeconds(31536000)
                            .includeSubDomains(true) // Correct method name
                        );
                    headers.referrerPolicy(policy ->
                        policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    );
                })

                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Admin endpoints
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")

                        // User endpoints
                        .requestMatchers(USER_ENDPOINTS).hasAnyRole("USER", "ADMIN")

                        // Protected product management endpoints
                        .requestMatchers("POST", "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers("PUT", "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers("PATCH", "/api/v1/products/**").hasRole("ADMIN")

                        // Protected user management endpoints
                        .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("POST", "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/v1/users/**").hasRole("ADMIN")

                        // Protected order endpoints
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authentication provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins
        configuration.setAllowedOriginPatterns(allowedOrigins);

        // Set allowed methods
        configuration.setAllowedMethods(allowedMethods);

        // Set allowed headers
        configuration.setAllowedHeaders(allowedHeaders);

        // Set exposed headers
        configuration.setExposedHeaders(exposedHeaders);

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Set max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}