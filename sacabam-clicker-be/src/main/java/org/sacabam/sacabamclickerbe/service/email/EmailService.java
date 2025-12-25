package org.sacabam.sacabamclickerbe.service.email;

public interface EmailService {

    /**
     * Gửi OTP qua email
     */
    void sendOtpEmail(String toEmail, String otpCode, String purpose);

    /**
     * Gửi email chào mừng sau khi đăng ký
     */
    void sendWelcomeEmail(String toEmail, String username);

    /**
     * Gửi email thông báo đổi mật khẩu thành công
     */
    void sendPasswordChangeNotification(String toEmail);
}