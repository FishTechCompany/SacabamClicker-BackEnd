package org.sacabam.sacabamclickerbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các endpoint của Auth module (login, register, otp...)
                        // Dựa trên axiosConfig.ts, các path này bắt đầu bằng /api/v1/auth/
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Cho phép xem tài liệu API Swagger/OpenAPI (nếu Goshujinsama cần)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Tất cả các request khác (như thao tác game) phải được xác thực
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}