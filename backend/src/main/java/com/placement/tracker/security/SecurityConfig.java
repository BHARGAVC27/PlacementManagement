package com.placement.tracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/register/student",
                    "/api/auth/login").permitAll()

                // Student
                .requestMatchers(HttpMethod.POST, "/api/applications")
                    .hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/applications/my")
                    .hasRole("STUDENT")

                // Admin
                .requestMatchers(HttpMethod.GET, "/api/applications/job/**")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.POST, "/api/jobs")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.DELETE, "/api/jobs/**")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.PUT, "/api/applications/*/status")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.POST, "/api/rounds")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // Swagger
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html").permitAll()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}