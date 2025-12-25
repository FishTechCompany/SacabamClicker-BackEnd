package org.sacabam.sacabamclickerbe.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.sacabam.sacabamclickerbe.dto.response.auth.ApiResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
    "spring.datasource.username=postgres",
    "spring.datasource.password=280604"
})
@Transactional // Rollback after each test - không tạo trash data
@DisplayName("Auth Integration Tests - Real Supabase Database")
class AuthSupabaseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private JsonNode getMockData() throws Exception {
        ClassPathResource resource = new ClassPathResource("mock-api-requests.json");
        return objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @DisplayName("Database Connection Test - Kiểm tra kết nối Supabase")
    void testSupabaseDatabaseConnection() {
        System.out.println("🔍 Testing Supabase Database Connection");

        // Kiểm tra kết nối database bằng cách đếm số lượng roles
        Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Roles\"", Integer.class);
        Integer permissionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Permissions\"", Integer.class);
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);

        System.out.println("📊 Supabase Database Status:");
        System.out.println("   - Roles: " + roleCount);
        System.out.println("   - Permissions: " + permissionCount);
        System.out.println("   - Users: " + userCount);

        // Verify basic data exists
        assertNotNull(roleCount, "Should be able to query Roles table");
        assertNotNull(permissionCount, "Should be able to query Permissions table");
        assertNotNull(userCount, "Should be able to query Users table");
        assertTrue(roleCount >= 3, "Should have at least 3 roles (USER, PRO, ADMIN)");
        assertTrue(permissionCount >= 7, "Should have at least 7 permissions");

        System.out.println("✅ Supabase Database Connection Successful");
    }

    @Test
    @DisplayName("Integration Test - Complete Auth Flow with Real Supabase Database")
    void completeAuthFlow_WithRealSupabaseDatabase() throws Exception {
        JsonNode mock = getMockData();

        System.out.println("🚀 Starting Complete Auth Flow Integration Test");

        // Count users before test
        Integer userCountBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);
        System.out.println("👥 Users in Supabase before test: " + userCountBefore);

        // 1. REGISTER - POST /auth/register
        String registerJson = objectMapper.writeValueAsString(mock.get("registerRequest"));

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.email").value("supabase.integration.test@example.com"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andReturn();

        // Parse register response
        String registerResponseJson = registerResult.getResponse().getContentAsString();
        ApiResponse<RegisterResponse> registerApiResponse = objectMapper.readValue(
                registerResponseJson,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, RegisterResponse.class)
        );

        Integer userId = registerApiResponse.getData().getUserId();
        System.out.println("✅ REGISTER successful - UserId: " + userId);

        // Verify user exists in Supabase database using JdbcTemplate
        Integer userCountAfterRegister = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);
        assertEquals(userCountBefore + 1, userCountAfterRegister, "User count should increase by 1");

        // Verify specific user data in database
        String userEmail = jdbcTemplate.queryForObject(
                "SELECT email FROM \"Users\" WHERE id = ?",
                String.class,
                userId
        );
        assertEquals("supabase.integration.test@example.com", userEmail, "User email should match");

        // Verify GameProfile was created
        Integer profileCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"GameProfiles\" WHERE \"userId\" = ?",
                Integer.class,
                userId
        );
        assertEquals(1, profileCount, "GameProfile should be created for user");

        // 2. LOGIN - POST /auth/login
        String loginJson = objectMapper.writeValueAsString(mock.get("loginRequest"));

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("supabase.integration.test@example.com"))
                .andReturn();

        // Parse login response to get token
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<LoginResponse> loginApiResponse = objectMapper.readValue(
                loginResponseJson,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, LoginResponse.class)
        );

        String accessToken = loginApiResponse.getData().getAccessToken();
        System.out.println("✅ LOGIN successful - Token received");

        // 3. RESYNC USER - POST /auth/resync-user
        JsonNode resyncRequest = mock.get("resyncUserRequest");
        ((com.fasterxml.jackson.databind.node.ObjectNode) resyncRequest).put("token", accessToken);
        String resyncJson = objectMapper.writeValueAsString(resyncRequest);

        mockMvc.perform(post("/auth/resync-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resyncJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đồng bộ dữ liệu thành công"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.email").value("supabase.integration.test@example.com"));

        System.out.println("✅ RESYNC successful");

        // Verify GameProfile data was updated in database
        Long currentScore = jdbcTemplate.queryForObject(
                "SELECT \"currentScore\" FROM \"GameProfiles\" WHERE \"userId\" = ?",
                Long.class,
                userId
        );
        assertEquals(99999L, currentScore, "Current score should be updated");

        // 4. FORGOT PASSWORD - POST /auth/forgot-password
        String forgotJson = objectMapper.writeValueAsString(mock.get("forgotPasswordRequest"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgotJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value("supabase.integration.test@example.com"));

        System.out.println("✅ FORGOT PASSWORD successful");

        // 5. RESET PASSWORD - PUT /auth/reset-password
        String resetJson = objectMapper.writeValueAsString(mock.get("resetPasswordRequest"));

        mockMvc.perform(put("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value("supabase.integration.test@example.com"));

        System.out.println("✅ RESET PASSWORD successful");

        // 6. LOGIN WITH NEW PASSWORD - POST /auth/login
        String loginAfterResetJson = objectMapper.writeValueAsString(mock.get("loginAfterResetRequest"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAfterResetJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.user.email").value("supabase.integration.test@example.com"));

        System.out.println("✅ LOGIN with NEW PASSWORD successful");

        // Final verification - user still exists before rollback
        Integer finalUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);
        assertEquals(userCountBefore + 1, finalUserCount, "User should still exist before rollback");

        System.out.println("🎉 COMPLETE INTEGRATION TEST SUCCESSFUL!");
        System.out.println("🔄 Transaction will rollback - No trash data in Supabase!");

        // Note: @Transactional will automatically rollback after this method
    }

    @Test
    @DisplayName("Integration Test - Rollback Verification with Supabase")
    void rollbackVerification_WithRealSupabase() throws Exception {
        System.out.println("🔍 Testing @Transactional Rollback with Real Supabase");

        // Count users before creating test data
        Integer userCountBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);
        System.out.println("📊 Users in Supabase before test: " + userCountBefore);

        // Create test user via API (will be rolled back)
        JsonNode mock = getMockData();
        String registerJson = objectMapper.writeValueAsString(mock.get("rollbackTestRequest"));

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify user was created in database
        Integer userCountAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Users\"", Integer.class);
        assertEquals(userCountBefore + 1, userCountAfter, "User should be created in Supabase");

        // Verify we can query the created user
        Integer createdUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"Users\" WHERE email = ?",
                Integer.class,
                "rollback.test.supabase@example.com"
        );
        assertEquals(1, createdUserCount, "Test user should exist in database during transaction");

        System.out.println("✅ Test user created in Supabase during transaction");
        System.out.println("📊 Users in Supabase during test: " + userCountAfter);
        System.out.println("🔄 Method ending - @Transactional will rollback from Supabase");

        // @Transactional will rollback when method ends
    }
}
