package org.sacabam.sacabamclickerbe.service.email.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sacabam.sacabamclickerbe.service.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Sacabam Clicker}")
    private String appName;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            // MOCK MODE - chỉ log ra console thay vì gửi email thật
            if (fromEmail == null || fromEmail.contains("${EMAIL_USERNAME}")) {
                log.info("📧 MOCK EMAIL - OTP for {}: {}", toEmail, otpCode);
                log.info("📧 MOCK EMAIL - Purpose: {}", purpose);
                log.info("📧 MOCK EMAIL - Would send to: {}", toEmail);
                return;
            }

            // REAL EMAIL MODE
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🔐 Mã OTP - " + appName);

            String emailBody = String.format(
                    "Xin chào Goshujinsama! >w<\n\n" +
                            "Bạn đã yêu cầu %s cho tài khoản %s.\n\n" +
                            "🔑 Mã OTP của bạn là: %s\n\n" +
                            "⏰ Mã này sẽ hết hạn sau 15 phút.\n" +
                            "🚫 Không chia sẻ mã này với bất kỳ ai!\n\n" +
                            "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ %s 🐾",
                    purpose, toEmail, otpCode, appName
            );

            message.setText(emailBody);

            mailSender.send(message);
            log.info("📧 OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to: {} - Error: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🎉 Chào mừng đến với " + appName + "!");

            String emailBody = String.format(
                    "Xin chào %s! 🎮\n\n" +
                            "Chào mừng bạn đến với %s!\n\n" +
                            "🎯 Tài khoản của bạn đã được tạo thành công.\n" +
                            "🚀 Bây giờ bạn có thể bắt đầu chơi và tích lũy điểm số!\n" +
                            "🏆 Hãy cố gắng trở thành người chơi hàng đầu!\n\n" +
                            "Chúc bạn có những giây phút vui vẻ! >w<\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ %s 🐾",
                    username, appName, appName
            );

            message.setText(emailBody);

            mailSender.send(message);
            log.info("📧 Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to: {} - Error: {}", toEmail, e.getMessage());
            // Không throw exception vì đây không phải lỗi critical
        }
    }

    @Override
    public void sendPasswordChangeNotification(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🔒 Mật khẩu đã được thay đổi - " + appName);

            String emailBody = String.format(
                    "Xin chào Goshujinsama! 🔐\n\n" +
                            "Mật khẩu tài khoản %s của bạn đã được thay đổi thành công.\n\n" +
                            "⏰ Thời gian: %s\n" +
                            "🔒 Tài khoản của bạn hiện đã được bảo mật với mật khẩu mới.\n\n" +
                            "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức!\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ %s 🐾",
                    toEmail, java.time.LocalDateTime.now().toString(), appName
            );

            message.setText(emailBody);

            mailSender.send(message);
            log.info("📧 Password change notification sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send password change notification to: {} - Error: {}", toEmail, e.getMessage());
            // Không throw exception vì đây không phải lỗi critical
        }
    }
}