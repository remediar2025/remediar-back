package com.remediar.back_remediar.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Error endpoint
                    .requestMatchers("/error").permitAll()
                    // Swagger/OpenAPI endpoints
                    .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    // Auth endpoints
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/password/forgot-password").permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/password/reset-password").permitAll()
                    // User endpoints
                    .requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasRole("USER")
                    .requestMatchers(HttpMethod.DELETE, "/usuarios/**").hasRole("USER")
                    .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                    // Public endpoints
                    .requestMatchers(HttpMethod.GET, "/dashboard/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/estoque/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/itens-estoque/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/medicamentos/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/pix/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
