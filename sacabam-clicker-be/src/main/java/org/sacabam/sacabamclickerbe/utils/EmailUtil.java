package org.sacabam.sacabamclickerbe.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class EmailUtil {

    // Regex nghiêm ngặt hơn cho email validation
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // Các domain phổ biến được chấp nhận
    private static final String[] COMMON_DOMAINS = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
            "icloud.com", "protonmail.com", "zoho.com", "aol.com",
            "live.com", "msn.com", "yandex.com", "mail.com"
    };

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);


    /**
     * Kiểm tra email có hợp lệ không (nghiêm ngặt hơn @Email annotation)
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        email = email.trim().toLowerCase();

        // Kiểm tra regex pattern
        if (!pattern.matcher(email).matches()) {
            log.warn("Email không đúng định dạng: {}", email);
            return false;
        }

        // Kiểm tra độ dài
        if (email.length() > 254) {
            log.warn("Email quá dài: {}", email);
            return false;
        }

        // Kiểm tra phần local (trước @)
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }

        String localPart = parts[0];
        String domainPart = parts[1];

        // Local part không được quá 64 ký tự
        if (localPart.length() > 64) {
            log.warn("Phần local của email quá dài: {}", email);
            return false;
        }

        // Không được bắt đầu hoặc kết thúc bằng dấu chấm
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            log.warn("Email không được bắt đầu/kết thúc bằng dấu chấm: {}", email);
            return false;
        }

        // Không được có hai dấu chấm liên tiếp
        if (localPart.contains("..")) {
            log.warn("Email không được có hai dấu chấm liên tiếp: {}", email);
            return false;
        }

        // Kiểm tra domain
        return isValidDomain(domainPart);
    }

    /**
     * Kiểm tra domain có hợp lệ không
     */
    private boolean isValidDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }

        domain = domain.trim().toLowerCase();

        // Domain không được bắt đầu hoặc kết thúc bằng dấu gạch ngang
        if (domain.startsWith("-") || domain.endsWith("-")) {
            log.warn("Domain không hợp lệ (bắt đầu/kết thúc bằng dấu gạch ngang): {}", domain);
            return false;
        }

        // Domain không được bắt đầu hoặc kết thúc bằng dấu chấm
        if (domain.startsWith(".") || domain.endsWith(".")) {
            log.warn("Domain không hợp lệ (bắt đầu/kết thúc bằng dấu chấm): {}", domain);
            return false;
        }

        // Kiểm tra có ít nhất một dấu chấm
        if (!domain.contains(".")) {
            log.warn("Domain phải có ít nhất một dấu chấm: {}", domain);
            return false;
        }

        // Kiểm tra TLD (Top Level Domain)
        String[] domainParts = domain.split("\\.");
        if (domainParts.length < 2) {
            return false;
        }

        // Kiểm tra từng phần của domain
        for (String part : domainParts) {
            if (part.isEmpty()) {
                log.warn("Domain có phần rỗng: {}", domain);
                return false;
            }

            // Mỗi phần không được bắt đầu hoặc kết thúc bằng dấu gạch ngang
            if (part.startsWith("-") || part.endsWith("-")) {
                log.warn("Domain có phần không hợp lệ (bắt đầu/kết thúc bằng dấu gạch ngang): {}", domain);
                return false;
            }

            // Mỗi phần chỉ được chứa chữ cái, số và dấu gạch ngang
            if (!part.matches("^[a-zA-Z0-9-]+$")) {
                log.warn("Domain có ký tự không hợp lệ: {}", domain);
                return false;
            }
        }

        String tld = domainParts[domainParts.length - 1];

        // TLD phải có ít nhất 2 ký tự và chỉ chứa chữ cái
        if (tld.length() < 2 || !tld.matches("^[a-zA-Z]+$")) {
            log.warn("TLD không hợp lệ: {}", tld);
            return false;
        }

        // Kiểm tra các trường hợp domain lạ như .com.com
        if (domain.endsWith(".com.com") || domain.endsWith(".net.net") ||
                domain.endsWith(".org.org") || domain.contains("..")) {
            log.warn("Domain có định dạng lạ: {}", domain);
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra email có phải từ domain phổ biến không
     */
    public boolean isCommonDomain(String email) {
        if (!isValidEmail(email)) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        for (String commonDomain : COMMON_DOMAINS) {
            if (domain.equals(commonDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Chuẩn hóa email (trim và lowercase)
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Lấy domain từ email
     */
    public String getDomain(String email) {
        if (!isValidEmail(email)) {
            return null;
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }
}