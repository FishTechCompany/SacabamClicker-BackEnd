package org.sacabam.sacabamclickerbe.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final String errorCode;
    private final Integer status;

    public AuthException(String message, String errorCode, Integer status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    // Common auth exceptions
    public static AuthException invalidCredentials() {
        return new AuthException("Email hoặc mật khẩu không chính xác", "INVALID_CREDENTIALS", 401);
    }

    public static AuthException userDisabled() {
        return new AuthException("Tài khoản đã bị vô hiệu hóa", "USER_DISABLED", 403);
    }

    public static AuthException invalidEmail() {
        return new AuthException("Định dạng email không hợp lệ", "INVALID_EMAIL", 400);
    }

    public static AuthException emailAlreadyExists() {
        return new AuthException("Email đã được sử dụng", "EMAIL_ALREADY_EXISTS", 409);
    }

    public static AuthException validationError(String message) {
        return new AuthException(message, "VALIDATION_ERROR", 400);
    }

    public static AuthException unauthorized() {
        return new AuthException("Token không hợp lệ hoặc đã hết hạn", "UNAUTHORIZED", 401);
    }

    public static AuthException accessDenied() {
        return new AuthException("Bạn không có quyền truy cập tài nguyên này", "ACCESS_DENIED", 403);
    }

    // OTP related exceptions
    public static AuthException userNotFound() {
        return new AuthException("Không tìm thấy người dùng với email này", "USER_NOT_FOUND", 404);
    }

    public static AuthException invalidOtp() {
        return new AuthException("Mã OTP không chính xác hoặc đã hết hạn!", "INVALID_OTP", 400);
    }

    public static AuthException otpExpired() {
        return new AuthException("OTP đã hết hạn", "OTP_EXPIRED", 400);
    }

    public static AuthException otpAlreadyUsed() {
        return new AuthException("OTP đã được sử dụng", "OTP_ALREADY_USED", 400);
    }

    public static AuthException otpRateLimitExceeded() {
        return new AuthException("Bạn đã yêu cầu OTP quá nhanh. Vui lòng thử lại sau 2 phút", "OTP_RATE_LIMIT_EXCEEDED", 429);
    }

    // Password related exceptions
    public static AuthException passwordMismatch() {
        return new AuthException("Mật khẩu xác nhận không khớp", "PASSWORD_MISMATCH", 400);
    }

    public static AuthException weakPassword() {
        return new AuthException("Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn", "WEAK_PASSWORD", 400);
    }

    // Token related exceptions
    public static AuthException invalidToken() {
        return new AuthException("Token không hợp lệ", "INVALID_TOKEN", 401);
    }

    public static AuthException tokenExpired() {
        return new AuthException("Token đã hết hạn", "TOKEN_EXPIRED", 401);
    }

    // User profile related exceptions
    public static AuthException profileNotFound() {
        return new AuthException("Không tìm thấy profile người dùng", "PROFILE_NOT_FOUND", 404);
    }

    public static AuthException profileUpdateFailed() {
        return new AuthException("Cập nhật profile thất bại", "PROFILE_UPDATE_FAILED", 500);
    }
}