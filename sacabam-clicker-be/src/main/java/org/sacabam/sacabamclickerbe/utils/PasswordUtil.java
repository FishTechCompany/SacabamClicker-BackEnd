package org.sacabam.sacabamclickerbe.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // Có thể thêm các rule khác như: chứa số, chữ hoa, ký tự đặc biệt
        return true;
    }
}