package com.gkshoppy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, com.gkshoppy.security.LoginSuccessHandler loginSuccessHandler) throws Exception {
        http
            // basic security for admin endpoints; public pages (shop, auth, api/search) remain accessible
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/shop", "/auth", "/api/**").permitAll()
                .anyRequest().permitAll()
            )
            // use form login at /auth with custom processing URL and parameter names
            .formLogin(form -> form
                .loginPage("/auth")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("emailOrUsername")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout.logoutUrl("/logout").permitAll())
            .csrf(); // CSRF protection enabled by default for state-changing requests

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
