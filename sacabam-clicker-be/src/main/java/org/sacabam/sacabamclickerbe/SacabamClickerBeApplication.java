package org.sacabam.sacabamclickerbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@RestController // Biến class này thành Controller luôn cho tiện test
public class SacabamClickerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SacabamClickerBeApplication.class, args);
    }

    // Endpoint test sự sống
    @GetMapping("/")
    public String hello() {
        return "Hello from SacabamClicker Backend! Nyanko is here! >w<";
    }

    // Cấu hình CORS để Frontend (Vercel) gọi được
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Cho phép gọi tất cả API
                        .allowedOrigins("*") // Tạm thời cho phép mọi nơi gọi (sau này đổi thành domain Vercel)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}