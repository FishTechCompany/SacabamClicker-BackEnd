package org.sacabam.sacabamclickerbe.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sacabam.sacabamclickerbe.dto.request.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.entity.GameProfile;
import org.sacabam.sacabamclickerbe.entity.Role;
import org.sacabam.sacabamclickerbe.entity.User;
import org.sacabam.sacabamclickerbe.enums.RoleName;
import org.sacabam.sacabamclickerbe.enums.UserStatus;
import org.sacabam.sacabamclickerbe.exception.AuthException;
import org.sacabam.sacabamclickerbe.mapper.AuthMapper;
import org.sacabam.sacabamclickerbe.repository.*;
import org.sacabam.sacabamclickerbe.service.impl.AuthServiceImpl;
import org.sacabam.sacabamclickerbe.utils.JwtUtil;
import org.sacabam.sacabamclickerbe.utils.PasswordUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Flow Simple Tests")
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

        // Mock password validation để pass qua validateRegisterRequest
        when(passwordUtil.isValidPassword(request.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.register(request));

        assertEquals("Email đã được sử dụng", exception.getMessage());
        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals(409, exception.getStatus());

        // Verify register flow stops at email check
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
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify register flow stops at validation
        verifyNoInteractions(userRepository, roleRepository, passwordUtil, authMapper, gameProfileRepository);
    }

    // ========== FORGOT PASSWORD FLOW TESTS ==========

    @Test
    @DisplayName("Forgot Password Flow - Xử lý thành công")
    void forgotPasswordFlow_Success() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> authService.forgotPassword(request));

        // Verify forgot password flow
        verify(userRepository).existsByEmail(request.getEmail());
    }

    @Test
    @DisplayName("Forgot Password Flow - Với email không tồn tại")
    void forgotPasswordFlow_WithNonExistentEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> authService.forgotPassword(request));

        // Verify forgot password flow (should not reveal if email exists)
        verify(userRepository).existsByEmail(request.getEmail());
    }

    // ========== RESET PASSWORD FLOW TESTS ==========

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

        assertEquals("Email không tồn tại", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify reset password flow stops at email check
        verify(passwordUtil).isValidPassword(request.getNewPassword());
        verify(userRepository).findByEmail(request.getEmail());
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
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify reset password flow stops at validation
        verifyNoInteractions(userRepository, passwordUtil);
    }

    // ========== RESYNC USER FLOW TESTS ==========

    @Test
    @DisplayName("Resync User Flow - Thất bại với userId không tồn tại")
    void resyncUserFlow_Failure_WithNonExistentUserId() {
        // Given
        ResyncUserRequest request = new ResyncUserRequest(999, 5000L, 3, 2);

        when(userRepository.findById(request.getUserId())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resyncUser(request));

        assertEquals("User không tồn tại", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify resync user flow stops at user check
        verify(userRepository).findById(request.getUserId());
        verifyNoInteractions(gameProfileRepository);
    }

    @Test
    @DisplayName("Resync User Flow - Thất bại với game profile không tồn tại")
    void resyncUserFlow_Failure_WithNonExistentGameProfile() {
        // Given
        ResyncUserRequest request = new ResyncUserRequest(1, 5000L, 3, 2);

        User testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");

        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(testUser));
        when(gameProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.resyncUser(request));

        assertEquals("Game profile không tồn tại", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getStatus());

        // Verify resync user flow stops at game profile check
        verify(userRepository).findById(request.getUserId());
        verify(gameProfileRepository).findByUserId(testUser.getId());
        verify(gameProfileRepository, never()).save(any(GameProfile.class));
    }
}