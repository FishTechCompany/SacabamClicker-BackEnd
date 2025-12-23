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
}