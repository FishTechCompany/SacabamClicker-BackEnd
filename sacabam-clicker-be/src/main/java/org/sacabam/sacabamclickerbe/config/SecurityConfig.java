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
                // 1. Tắt CSRF để Frontend có thể gửi POST/PUT request mà không cần token
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Tắt CORS để tránh lỗi trình duyệt chặn request từ IP khác
                .cors(AbstractHttpConfigurer::disable)

                // 3. Cho phép TẤT CẢ mọi request không cần đăng nhập
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 4. Đảm bảo không yêu cầu xác thực cơ bản (Form login)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}