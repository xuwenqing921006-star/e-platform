package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * health profile 的最小安全链。
 */
@Configuration
@Profile("health")
public class HealthSecurityConfig
{
    @Bean
    protected SecurityFilterChain healthFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/health", "/error").permitAll()
                .anyRequest().denyAll())
            .build();
    }
}
