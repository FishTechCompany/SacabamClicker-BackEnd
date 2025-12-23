package org.sacabam.sacabamclickerbe.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.sacabam.sacabamclickerbe.dto.request.auth.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.auth.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;
import org.sacabam.sacabamclickerbe.service.auth.AuthService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Rollback after each test - không tạo trash data
@DisplayName("Auth Integration Tests - Real Database")
class AuthIntegrationTestReal {

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode getMockData() throws Exception {
        ClassPathResource resource = new ClassPathResource("mock-api-requests.json");
        return objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @DisplayName("Integration Test - Complete Auth Flow với Real Supabase Database")
    void completeAuthFlow_WithRealDatabase() throws Exception {
        JsonNode mock = getMockData();

        System.out.println("🚀 Starting Integration Test with Real Supabase Database");

        // 1. REGISTER - Tạo tài khoản mới
        RegisterRequest registerRequest = objectMapper.treeToValue(
                mock.get("registerRequest"), RegisterRequest.class);

        RegisterResponse registerResponse = authService.register(registerRequest);

        assertNotNull(registerResponse);
        assertEquals("integration.test@example.com", registerResponse.getEmail());
        assertNotNull(registerResponse.getUserId());

        Integer userId = registerResponse.getUserId();
        System.out.println("✅ REGISTER successful - UserId: " + userId);

        // 2. LOGIN - Đăng nhập với tài khoản vừa tạo
        LoginRequest loginRequest = objectMapper.treeToValue(
                mock.get("loginRequest"), LoginRequest.class);

        LoginResponse loginResponse = authService.login(loginRequest);

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        assertEquals("integration.test@example.com", loginResponse.getUser().getEmail());
        assertEquals("USER", loginResponse.getUser().getRole().getName());
        assertEquals("Player" + userId, loginResponse.getUser().getProfile().getDisplayName());
        assertTrue(loginResponse.getUser().getPermissions().contains("PLAY_GAME"));

        System.out.println("✅ LOGIN successful - Token: " + loginResponse.getAccessToken().substring(0, 20) + "...");
        System.out.println("   User: " + loginResponse.getUser().getEmail());
        System.out.println("   Role: " + loginResponse.getUser().getRole().getName());
        System.out.println("   Permissions: " + loginResponse.getUser().getPermissions());

        // 3. RESYNC USER - Cập nhật game profile
        ResyncUserRequest resyncRequest = new ResyncUserRequest();
        resyncRequest.setUserId(userId);
        resyncRequest.setCurrentScore(9999L);
        resyncRequest.setClickPower(5);
        resyncRequest.setUpgradeLevel(3);

        assertDoesNotThrow(() -> authService.resyncUser(resyncRequest));
        System.out.println("✅ RESYNC successful - Updated score: 9999, power: 5, level: 3");

        // 4. LOGIN LẠI - Kiểm tra dữ liệu đã được cập nhật
        LoginResponse loginAfterResync = authService.login(loginRequest);

        assertEquals(9999L, loginAfterResync.getUser().getProfile().getCurrentScore());
        assertEquals(5, loginAfterResync.getUser().getProfile().getClickPower());
        assertEquals(3, loginAfterResync.getUser().getProfile().getUpgradeLevel());

        System.out.println("✅ LOGIN after RESYNC - Data updated successfully!");
        System.out.println("   Score: " + loginAfterResync.getUser().getProfile().getCurrentScore());
        System.out.println("   Click Power: " + loginAfterResync.getUser().getProfile().getClickPower());
        System.out.println("   Upgrade Level: " + loginAfterResync.getUser().getProfile().getUpgradeLevel());

        // 5. FORGOT PASSWORD - Gửi OTP
        ForgotPasswordRequest forgotRequest = objectMapper.treeToValue(
                mock.get("forgotPasswordRequest"), ForgotPasswordRequest.class);

        assertDoesNotThrow(() -> authService.forgotPassword(forgotRequest));
        System.out.println("✅ FORGOT PASSWORD successful");

        // 6. RESET PASSWORD - Đổi mật khẩu
        ResetPasswordRequest resetRequest = objectMapper.treeToValue(
                mock.get("resetPasswordRequest"), ResetPasswordRequest.class);

        assertDoesNotThrow(() -> authService.resetPassword(resetRequest));
        System.out.println("✅ RESET PASSWORD successful");

        // 7. LOGIN VỚI MẬT KHẨU MỚI - Xác nhận reset password thành công
        LoginRequest loginAfterResetRequest = objectMapper.treeToValue(
                mock.get("loginAfterResetRequest"), LoginRequest.class);

        LoginResponse loginAfterReset = authService.login(loginAfterResetRequest);

        assertNotNull(loginAfterReset);
        assertEquals("integration.test@example.com", loginAfterReset.getUser().getEmail());

        System.out.println("✅ LOGIN with NEW PASSWORD successful");

        // Test hoàn thành - Transaction sẽ tự động rollback
        System.out.println("🎉 COMPLETE INTEGRATION TEST SUCCESSFUL!");
        System.out.println("🔄 Transaction will rollback - No trash data in Supabase!");
    }

    @Test
    @DisplayName("Integration Test - Error Cases với Real Database")
    void errorCases_WithRealDatabase() throws Exception {
        JsonNode mock = getMockData();

        System.out.println("🚀 Starting Error Cases Test with Real Supabase Database");

        // 1. LOGIN với email không tồn tại
        LoginRequest loginRequest = objectMapper.treeToValue(
                mock.get("loginRequest"), LoginRequest.class);

        Exception loginException = assertThrows(Exception.class, () -> authService.login(loginRequest));
        System.out.println("✅ LOGIN with non-existent email failed as expected: " + loginException.getMessage());

        // 2. REGISTER tài khoản
        RegisterRequest registerRequest = objectMapper.treeToValue(
                mock.get("registerRequest"), RegisterRequest.class);

        RegisterResponse registerResponse = authService.register(registerRequest);
        assertNotNull(registerResponse);
        System.out.println("✅ REGISTER successful - UserId: " + registerResponse.getUserId());

        // 3. REGISTER lại với email đã tồn tại
        Exception registerException = assertThrows(Exception.class, () -> authService.register(registerRequest));
        System.out.println("✅ REGISTER with existing email failed as expected: " + registerException.getMessage());

        // 4. RESYNC USER với userId không tồn tại
        ResyncUserRequest resyncRequest = new ResyncUserRequest();
        resyncRequest.setUserId(999); // Non-existent userId
        resyncRequest.setCurrentScore(1000L);

        Exception resyncException = assertThrows(Exception.class, () -> authService.resyncUser(resyncRequest));
        System.out.println("✅ RESYNC with non-existent userId failed as expected: " + resyncException.getMessage());

        // 5. RESET PASSWORD với email không tồn tại
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setEmail("nonexistent@example.com");
        resetRequest.setOtp("123456");
        resetRequest.setNewPassword("newpassword123");
        resetRequest.setConfirmPassword("newpassword123");

        Exception resetException = assertThrows(Exception.class, () -> authService.resetPassword(resetRequest));
        System.out.println("✅ RESET PASSWORD with non-existent email failed as expected: " + resetException.getMessage());

        System.out.println("🎉 ERROR CASES TEST SUCCESSFUL!");
        System.out.println("🔄 Transaction will rollback - No trash data in Supabase!");
    }
}