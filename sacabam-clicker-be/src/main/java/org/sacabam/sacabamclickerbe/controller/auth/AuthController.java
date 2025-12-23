package org.sacabam.sacabamclickerbe.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sacabam.sacabamclickerbe.dto.request.auth.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.auth.ApiResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;
import org.sacabam.sacabamclickerbe.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "API xác thực người dùng")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email và mật khẩu")
    @PostMapping("/admin/users")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(loginResponse, "Đăng nhập thành công");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đăng ký", description = "Tạo tài khoản mới")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse registerResponse = authService.register(request);
        ApiResponse<RegisterResponse> response = ApiResponse.created(registerResponse, "Đăng ký thành công");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Quên mật khẩu", description = "Gửi OTP để reset mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        ApiResponse<Object> response = ApiResponse.success(null, "Nếu email tồn tại, OTP sẽ được gửi");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset mật khẩu", description = "Reset mật khẩu bằng OTP")
    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        ApiResponse<Object> response = ApiResponse.success(null, "Đổi mật khẩu thành công");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đồng bộ dữ liệu user", description = "Cập nhật thông tin game profile của user")
    @PostMapping("/auth/me")
    public ResponseEntity<ApiResponse<Object>> resyncUser(@Valid @RequestBody ResyncUserRequest request) {
        authService.resyncUser(request);
        ApiResponse<Object> response = ApiResponse.success(null, "Đồng bộ dữ liệu thành công");
        return ResponseEntity.ok(response);
    }
}