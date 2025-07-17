package io.github.kianfatemi.code_review_assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/login**", "/webjars/**", "/error**", "/api/webhook/**").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(withDefaults());

        http.csrf(csrf ->
                csrf.ignoringRequestMatchers("/api/webhook/**")
        );
        return http.build();
    }
}