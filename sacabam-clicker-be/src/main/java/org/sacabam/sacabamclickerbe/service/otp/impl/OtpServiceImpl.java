package org.sacabam.sacabamclickerbe.service.otp.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sacabam.sacabamclickerbe.enums.auth.OtpType;
import org.sacabam.sacabamclickerbe.exception.AuthException;
import org.sacabam.sacabamclickerbe.service.email.EmailService;
import org.sacabam.sacabamclickerbe.service.otp.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final EmailService emailService;

    // Lưu OTP: key = "email:FORGOT_PASSWORD", value = "otpCode|expiryTime"
    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    // Rate limiting: key = "email:FORGOT_PASSWORD", value = lastRequestTime
    private final ConcurrentHashMap<String, LocalDateTime> rateLimitStorage = new ConcurrentHashMap<>();

    @Value("${otp.expiration.minutes:15}")
    private int otpExpirationMinutes;

    @Value("${otp.rate.limit.minutes:2}")
    private int rateLimitMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void generateAndSendOtp(String email, OtpType otpType) {
        log.info("🔐 Generating OTP for email: {} with type: {}", email, otpType);

        String key = email.toLowerCase() + ":" + otpType.name();

        // Kiểm tra rate limiting
        checkRateLimit(key, email);

        // Tạo OTP code 6 số
        String otpCode = generateOtpCode();

        // Tạo expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Lưu OTP với format: "otpCode|expiryTime" (dùng | thay vì : để tránh conflict với timestamp)
        String otpValue = otpCode + "|" + expiresAt.toString();
        otpStorage.put(key, otpValue);

        // Debug log để kiểm tra format
        log.info("🔍 OTP Debug - Key: {}, Value: {}", key, otpValue);

        // Cập nhật rate limit
        rateLimitStorage.put(key, LocalDateTime.now());

        // Gửi email
        String purpose = getOtpPurposeText(otpType);
        emailService.sendOtpEmail(email, otpCode, purpose);

        log.info("✅ OTP generated and sent successfully for email: {} (expires at: {})", email, expiresAt);
    }

    @Override
    public boolean validateOtp(String email, String otpCode, OtpType otpType) {
        log.info("🔍 Validating OTP for email: {} with type: {}", email, otpType);

        String key = email.toLowerCase() + ":" + otpType.name();
        String otpValue = otpStorage.get(key);

        // Debug log để kiểm tra giá trị trong storage
        log.info("🔍 OTP Debug - Key: {}, Stored Value: {}", key, otpValue);

        if (otpValue == null) {
            log.warn("❌ OTP not found for email: {}", email);
            return false;
        }

        // Parse otpValue: "otpCode|expiryTime"
        String[] parts = otpValue.split("\\|");
        log.info("🔍 OTP Debug - Split parts: {} (length: {})", java.util.Arrays.toString(parts), parts.length);

        if (parts.length != 2) {
            log.error("❌ Invalid OTP format in storage for email: {} - Expected 2 parts, got {}", email, parts.length);
            otpStorage.remove(key);
            return false;
        }

        String storedOtpCode = parts[0];
        LocalDateTime expiresAt;

        try {
            expiresAt = LocalDateTime.parse(parts[1]);
        } catch (Exception e) {
            log.error("❌ Failed to parse expiry time for email: {} - {}", email, e.getMessage());
            otpStorage.remove(key);
            return false;
        }

        // Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(expiresAt)) {
            log.warn("❌ OTP expired for email: {} (expired at: {})", email, expiresAt);
            otpStorage.remove(key);
            rateLimitStorage.remove(key);
            return false;
        }

        // Kiểm tra OTP code
        boolean isValid = storedOtpCode.equals(otpCode);

        if (isValid) {
            log.info("✅ OTP validated successfully for email: {}", email);
            // Xóa OTP sau khi sử dụng thành công
            otpStorage.remove(key);
            rateLimitStorage.remove(key);
        } else {
            log.warn("❌ Invalid OTP code for email: {} - Expected: {}, Got: {}", email, storedOtpCode, otpCode);
        }

        return isValid;
    }

    @Override
    public void cleanupExpiredOtps() {
        log.info("🧹 Cleaning up expired OTPs from memory...");

        LocalDateTime now = LocalDateTime.now();

        // Cleanup expired OTPs
        otpStorage.entrySet().removeIf(entry -> {
            String[] parts = entry.getValue().split("\\|");
            if (parts.length == 2) {
                LocalDateTime expiresAt = LocalDateTime.parse(parts[1]);
                if (now.isAfter(expiresAt)) {
                    rateLimitStorage.remove(entry.getKey());
                    return true;
                }
            }
            return false;
        });

        // Cleanup old rate limit entries
        LocalDateTime rateLimitCutoff = now.minusMinutes(rateLimitMinutes);
        rateLimitStorage.entrySet().removeIf(entry ->
                entry.getValue().isBefore(rateLimitCutoff)
        );

        log.info("✅ Memory cleanup completed. Active OTPs: {}, Rate limits: {}",
                otpStorage.size(), rateLimitStorage.size());
    }

    private void checkRateLimit(String key, String email) {
        LocalDateTime lastRequest = rateLimitStorage.get(key);

        if (lastRequest != null) {
            LocalDateTime rateLimitTime = lastRequest.plusMinutes(rateLimitMinutes);

            if (LocalDateTime.now().isBefore(rateLimitTime)) {
                log.warn("⚠️ Rate limit exceeded for email: {} - Last request at: {}", email, lastRequest);
                throw AuthException.otpRateLimitExceeded();
            }
        }
    }

    private String generateOtpCode() {
        // Tạo OTP 6 số ngẫu nhiên
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    private String getOtpPurposeText(OtpType otpType) {
        return switch (otpType) {
            case FORGOT_PASSWORD -> "đặt lại mật khẩu";
        };
    }

    // Debug method
    public void printMemoryStatus() {
        log.info("📊 OTP Memory Status - Active OTPs: {}, Rate limits: {}",
                otpStorage.size(), rateLimitStorage.size());

        otpStorage.forEach((key, value) -> {
            String[] parts = value.split("\\|");
            if (parts.length == 2) {
                log.info("  - {}: {} (expires: {})", key, parts[0], parts[1]);
            }
        });
    }

    // Debug method để kiểm tra OTP có tồn tại không
    public boolean hasOtp(String email, OtpType otpType) {
        String key = email.toLowerCase() + ":" + otpType.name();
        return otpStorage.containsKey(key);
    }

    // Debug method để lấy thông tin OTP
    public String getOtpInfo(String email, OtpType otpType) {
        String key = email.toLowerCase() + ":" + otpType.name();
        String otpValue = otpStorage.get(key);

        if (otpValue == null) {
            return "OTP not found for " + email;
        }

        String[] parts = otpValue.split("\\|");
        if (parts.length == 2) {
            return String.format("OTP: %s, Expires: %s", parts[0], parts[1]);
        }

        return "Invalid OTP format: " + otpValue;
    }
}