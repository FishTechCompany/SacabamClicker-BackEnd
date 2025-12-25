package org.sacabam.sacabamclickerbe.service.otp;

import org.sacabam.sacabamclickerbe.enums.auth.OtpType;

public interface OtpService {

    /**
     * Tạo và gửi OTP qua email
     */
    void generateAndSendOtp(String email, OtpType otpType);

    /**
     * Xác thực OTP
     */
    boolean validateOtp(String email, String otpCode, OtpType otpType);

    /**
     * Xóa OTP đã hết hạn (cleanup job)
     */
    void cleanupExpiredOtps();
}