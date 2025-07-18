package io.github.kianfatemi.code_review_assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "/webjars/**", "/api/webhook/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Configure OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                )
                
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        http.csrf(csrf ->
                csrf.ignoringRequestMatchers("/api/webhook/**")
        );

        return http.build();
    }
}
