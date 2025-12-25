package org.sacabam.sacabamclickerbe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sacabam.sacabamclickerbe.dto.request.auth.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.auth.ForgotPasswordResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.ResetPasswordResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.ResyncUserResponse;
import org.sacabam.sacabamclickerbe.entity.GameProfile;
import org.sacabam.sacabamclickerbe.entity.Role;
import org.sacabam.sacabamclickerbe.entity.User;
import org.sacabam.sacabamclickerbe.enums.auth.OtpType;
import org.sacabam.sacabamclickerbe.enums.auth.RoleName;
import org.sacabam.sacabamclickerbe.enums.auth.UserStatus;
import org.sacabam.sacabamclickerbe.exception.AuthException;
import org.sacabam.sacabamclickerbe.mapper.auth.AuthMapper;
import org.sacabam.sacabamclickerbe.repository.*;
import org.sacabam.sacabamclickerbe.service.auth.impl.AuthServiceImpl;
import org.sacabam.sacabamclickerbe.service.email.EmailService;
import org.sacabam.sacabamclickerbe.service.otp.OtpService;
import org.sacabam.sacabamclickerbe.utils.JwtUtil;
import org.sacabam.sacabamclickerbe.utils.PasswordUtil;
import org.sacabam.sacabamclickerbe.utils.EmailUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Flow Complete Tests with OTP")
class AuthFlowSimpleTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GameProfileRepository gameProfileRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailUtil emailUtil;

    @BeforeEach
    void setUp() {
        // Mock EmailUtil để trả về true cho tất cả email hợp lệ trong test (lenient để tránh unnecessary stubbing warning)
        lenient().when(emailUtil.isValidEmail(anyString())).thenReturn(true);
        lenient().when(emailUtil.normalizeEmail(anyString())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).toLowerCase());
    }

    @InjectMocks
    private AuthServiceImpl authService;

    // ========== LOGIN FLOW TESTS ==========

    @Test
    @DisplayName("Login Flow - Thất bại với email không tồn tại")
    void loginFlow_Failure_WithNonExistentEmail() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        when(userRepository.findByEmailWithRole(request.getEmail()))
                .thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request));

        assertEquals("Email hoặc mật khẩu không chính xác", exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertEquals(401, exception.getStatus());

        // Verify auth flow stops at first step
        verify(userRepository).findByEmailWithRole(request.getEmail());
        verifyNoInteractions(passwordUtil, gameProfileRepository, rolePermissionRepository, jwtUtil);
    }

    @Test
    @DisplayName("Login Flow - Thất bại với mật khẩu sai")
    void loginFlow_Failure_WithWrongPassword() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        Role testRole = new Role();
        testRole.setId(1);
        testRole.setName(RoleName.USER.getValue());

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(testRole);
        testUser.setStatus(UserStatus.ACTIVE.getValue());

        when(userRepository.findByEmailWithRole(request.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordUtil.matches(request.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request));

        assertEquals("Email hoặc mật khẩu không chính xác", exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertEquals(401, exception.getStatus());

        // Verify auth flow stops at password check
        verify(userRepository).findByEmailWithRole(request.getEmail());
        verify(passwordUtil).matches(request.getPassword(), testUser.getPassword());
        verifyNoInteractions(gameProfileRepository, rolePermissionRepository, jwtUtil);
    }

    @Test
    @DisplayName("Login Flow - Thất bại với tài khoản bị vô hiệu hóa")
    void loginFlow_Failure_WithDisabledUser() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        Role testRole = new Role();
        testRole.setId(1);
        testRole.setName(RoleName.USER.getValue());

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(testRole);
        testUser.setStatus(UserStatus.INACTIVE.getValue()); // Disabled user

        when(userRepository.findByEmailWithRole(request.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordUtil.matches(request.getPassword(), testUser.getPassword()))
                .thenReturn(true);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request));

        assertEquals("Tài khoản đã bị vô hiệu hóa", exception.getMessage());
        assertEquals("USER_DISABLED", exception.getErrorCode());
        assertEquals(403, exception.getStatus());

        // Verify auth flow stops at status check
        verify(userRepository).findByEmailWithRole(request.getEmail());
        verify(passwordUtil).matches(request.getPassword(), testUser.getPassword());
        verifyNoInteractions(gameProfileRepository, rolePermissionRepository, jwtUtil);
    }

    // ========== REGISTER FLOW TESTS ==========

    @Test
    @DisplayName("Register Flow - Thất bại với email đã tồn tại")
    void registerFlow_Failure_WithExistingEmail() {
        // Given
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "password123");

        // Mock email validation và password validation để pass qua validateRegisterRequest
        when(emailUtil.isValidEmail(request.getEmail())).thenReturn(true);
        when(passwordUtil.isValidPassword(request.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.register(request));

        assertEquals("Email đã được sử dụng", exception.getMessage());
        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals(409, exception.getStatus());

        // Verify register flow stops at email check
        verify(emailUtil).isValidEmail(request.getEmail());
        verify(passwordUtil).isValidPassword(request.getPassword());
        verify(userRepository).existsByEmail(request.getEmail());
        verifyNoInteractions(roleRepository, authMapper, gameProfileRepository);
    }

    @Test
    @DisplayName("Register Flow - Thất bại với mật khẩu xác nhận không khớp")
    void registerFlow_Failure_WithPasswordMismatch() {
        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "differentpassword");

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.register(request));

        assertEquals("Mật khẩu xác nhận không khớp", exception.getMessage());
        assertEquals("PASSWORD_MISMATCH", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify register flow stops at validation
        verifyNoInteractions(userRepository, roleRepository, passwordUtil, authMapper, gameProfileRepository);
    }

    // ========== FORGOT PASSWORD FLOW TESTS WITH OTP ==========

    @Test
    @DisplayName("Forgot Password Flow - Thành công với user tồn tại (gửi OTP thật)")
    void forgotPasswordFlow_Success_WithExistingUser() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        doNothing().when(otpService).generateAndSendOtp(request.getEmail(), OtpType.FORGOT_PASSWORD);

        // When
        ForgotPasswordResponse response = authService.forgotPassword(request);

        // Then
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("OTP đã được gửi đến email của bạn", response.getMessage());
        assertEquals(15, response.getOtpExpirationMinutes());

        // Verify OTP service was called
        verify(userRepository).existsByEmail(request.getEmail());
        verify(otpService).generateAndSendOtp(request.getEmail(), OtpType.FORGOT_PASSWORD);
    }

    @Test
    @DisplayName("Forgot Password Flow - Với email không tồn tại (không gửi OTP)")
    void forgotPasswordFlow_WithNonExistentEmail_NoOtpSent() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // When
        ForgotPasswordResponse response = authService.forgotPassword(request);

        // Then
        assertNotNull(response);
        assertEquals("nonexistent@example.com", response.getEmail());
        assertEquals("Nếu email tồn tại, OTP sẽ được gửi đến email của bạn", response.getMessage());
        assertEquals(15, response.getOtpExpirationMinutes());

        // Verify OTP service was NOT called for non-existent email
        verify(userRepository).existsByEmail(request.getEmail());
        verify(otpService, never()).generateAndSendOtp(anyString(), any(OtpType.class));
    }

    // ========== RESET PASSWORD FLOW TESTS WITH OTP ==========

    @Test
    @DisplayName("Reset Password Flow - Thành công với OTP hợp lệ")
    void resetPasswordFlow_Success_WithValidOtp() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newpassword123", "newpassword123");

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("oldHashedPassword");

        when(passwordUtil.isValidPassword(request.getNewPassword())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD)).thenReturn(true);
        when(passwordUtil.encode(request.getNewPassword())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordChangeNotification(testUser.getEmail());

        // When
        ResetPasswordResponse response = authService.resetPassword(request);

        // Then
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Mật khẩu đã được cập nhật thành công", response.getMessage());

        // Verify complete flow
        verify(passwordUtil).isValidPassword(request.getNewPassword());
        verify(userRepository).findByEmail(request.getEmail());
        verify(otpService).validateOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD);
        verify(passwordUtil).encode(request.getNewPassword());
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordChangeNotification(testUser.getEmail());
    }

    @Test
    @DisplayName("Reset Password Flow - Thất bại với OTP không hợp lệ")
    void resetPasswordFlow_Failure_WithInvalidOtp() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "wrong-otp", "newpassword123", "newpassword123");

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");

        when(passwordUtil.isValidPassword(request.getNewPassword())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD)).thenReturn(false);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resetPassword(request));

        assertEquals("Mã OTP không chính xác hoặc đã hết hạn!", exception.getMessage());
        assertEquals("INVALID_OTP", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify flow stops at OTP validation
        verify(passwordUtil).isValidPassword(request.getNewPassword());
        verify(userRepository).findByEmail(request.getEmail());
        verify(otpService).validateOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD);
        verify(passwordUtil, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordChangeNotification(anyString());
    }

    @Test
    @DisplayName("Reset Password Flow - Thất bại với email không tồn tại")
    void resetPasswordFlow_Failure_WithNonExistentEmail() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("nonexistent@example.com", "123456", "newpassword123", "newpassword123");

        when(passwordUtil.isValidPassword(request.getNewPassword())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resetPassword(request));

        assertEquals("Không tìm thấy người dùng với email này", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        // Verify reset password flow stops at email check
        verify(passwordUtil).isValidPassword(request.getNewPassword());
        verify(userRepository).findByEmail(request.getEmail());
        verify(otpService, never()).validateOtp(anyString(), anyString(), any(OtpType.class));
        verify(passwordUtil, never()).encode(anyString());
    }

    @Test
    @DisplayName("Reset Password Flow - Thất bại với mật khẩu xác nhận không khớp")
    void resetPasswordFlow_Failure_WithPasswordMismatch() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newpassword123", "differentpassword");

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resetPassword(request));

        assertEquals("Mật khẩu xác nhận không khớp", exception.getMessage());
        assertEquals("PASSWORD_MISMATCH", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify reset password flow stops at validation
        verifyNoInteractions(userRepository, passwordUtil);
    }

    // ========== RESYNC USER FLOW TESTS ==========

    @Test
    @DisplayName("Resync User Flow - Thất bại với token không hợp lệ")
    void resyncUserFlow_Failure_WithInvalidToken() {
        // Given
        ResyncUserRequest request = new ResyncUserRequest("invalid-token", 5000L, 3, 2);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resyncUser(request));

        assertEquals("Token không hợp lệ", exception.getMessage());
        assertEquals("INVALID_TOKEN", exception.getErrorCode());
        assertEquals(401, exception.getStatus());

        // Verify no database interactions for invalid token
        verifyNoInteractions(userRepository);
        verifyNoInteractions(gameProfileRepository);
    }

    @Test
    @DisplayName("Resync User Flow - Thành công với token hợp lệ")
    void resyncUserFlow_Success_WithValidToken() {
        // Given - Create a valid JWT token for testing
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.test";
        ResyncUserRequest request = new ResyncUserRequest(validToken, 5000L, 3, 2);

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");

        GameProfile testProfile = new GameProfile();
        testProfile.setId(1);
        testProfile.setUser(testUser); // Sử dụng setUser thay vì setUserId
        testProfile.setCurrentScore(1000L);
        testProfile.setClickPower(1);
        testProfile.setUpgradeLevel(1);

        // Mock JWT validation and user lookup
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(validToken)).thenReturn(1);
        when(jwtUtil.getEmailFromToken(validToken)).thenReturn("test@example.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(gameProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));
        when(gameProfileRepository.save(any(GameProfile.class))).thenReturn(testProfile);

        // When
        ResyncUserResponse response = authService.resyncUser(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(5000L, response.getCurrentScore());
        assertEquals(3, response.getClickPower());
        assertEquals(2, response.getUpgradeLevel());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(gameProfileRepository).findByUserId(testUser.getId());
        verify(gameProfileRepository).save(any(GameProfile.class));
    }

    // ========== COMPLETE OTP FLOW TESTS ==========

    @Test
    @DisplayName("Complete OTP Flow - Forgot Password → Reset Password thành công")
    void completeOtpFlow_ForgotToReset_Success() {
        // Given
        String testEmail = "test@example.com";
        String testOtp = "123456";

        // Step 1: Forgot Password
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest(testEmail);

        // Step 2: Reset Password
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(testEmail, testOtp, "newpassword123", "newpassword123");

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail(testEmail);
        testUser.setPassword("oldHashedPassword");

        // Mock forgot password flow
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);
        doNothing().when(otpService).generateAndSendOtp(testEmail, OtpType.FORGOT_PASSWORD);

        // Mock reset password flow
        when(passwordUtil.isValidPassword(resetRequest.getNewPassword())).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testEmail, testOtp, OtpType.FORGOT_PASSWORD)).thenReturn(true);
        when(passwordUtil.encode(resetRequest.getNewPassword())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordChangeNotification(testEmail);

        // When - Execute complete flow
        // Step 1: User forgets password
        ForgotPasswordResponse forgotResponse = authService.forgotPassword(forgotRequest);

        // Step 2: User resets password with OTP
        ResetPasswordResponse resetResponse = authService.resetPassword(resetRequest);

        // Then - Verify both steps
        // Forgot Password Response
        assertNotNull(forgotResponse);
        assertEquals(testEmail, forgotResponse.getEmail());
        assertEquals("OTP đã được gửi đến email của bạn", forgotResponse.getMessage());

        // Reset Password Response
        assertNotNull(resetResponse);
        assertEquals(testEmail, resetResponse.getEmail());
        assertEquals("Mật khẩu đã được cập nhật thành công", resetResponse.getMessage());

        // Verify complete flow interactions
        verify(userRepository).existsByEmail(testEmail);
        verify(otpService).generateAndSendOtp(testEmail, OtpType.FORGOT_PASSWORD);
        verify(userRepository).findByEmail(testEmail);
        verify(otpService).validateOtp(testEmail, testOtp, OtpType.FORGOT_PASSWORD);
        verify(passwordUtil).encode(resetRequest.getNewPassword());
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordChangeNotification(testEmail);
    }

    @Test
    @DisplayName("Complete OTP Flow - Rate Limiting Test")
    void completeOtpFlow_RateLimiting_Test() {
        // Given
        String testEmail = "test@example.com";
        ForgotPasswordRequest request = new ForgotPasswordRequest(testEmail);

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Mock rate limit exceeded
        doThrow(AuthException.otpRateLimitExceeded())
                .when(otpService).generateAndSendOtp(testEmail, OtpType.FORGOT_PASSWORD);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.forgotPassword(request));

        assertEquals("Bạn đã yêu cầu OTP quá nhanh. Vui lòng thử lại sau 2 phút", exception.getMessage());
        assertEquals("OTP_RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(429, exception.getStatus());

        // Verify rate limiting was checked
        verify(userRepository).existsByEmail(testEmail);
        verify(otpService).generateAndSendOtp(testEmail, OtpType.FORGOT_PASSWORD);
    }
}