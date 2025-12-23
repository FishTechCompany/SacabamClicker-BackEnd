package org.sacabam.sacabamclickerbe.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sacabam.sacabamclickerbe.dto.request.auth.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.auth.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;
import org.sacabam.sacabamclickerbe.entity.GameProfile;
import org.sacabam.sacabamclickerbe.entity.Role;
import org.sacabam.sacabamclickerbe.entity.RolePermission;
import org.sacabam.sacabamclickerbe.entity.User;
import org.sacabam.sacabamclickerbe.enums.auth.GameProfileStatus;
import org.sacabam.sacabamclickerbe.enums.auth.RoleName;
import org.sacabam.sacabamclickerbe.enums.auth.UserStatus;
import org.sacabam.sacabamclickerbe.exception.AuthException;
import org.sacabam.sacabamclickerbe.mapper.auth.AuthMapper;
import org.sacabam.sacabamclickerbe.repository.GameProfileRepository;
import org.sacabam.sacabamclickerbe.repository.RolePermissionRepository;
import org.sacabam.sacabamclickerbe.repository.RoleRepository;
import org.sacabam.sacabamclickerbe.repository.UserRepository;
import org.sacabam.sacabamclickerbe.service.auth.AuthService;
import org.sacabam.sacabamclickerbe.utils.JwtUtil;
import org.sacabam.sacabamclickerbe.utils.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GameProfileRepository gameProfileRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuthMapper authMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        // Tìm user với role
        User user = userRepository.findByEmailWithRole(request.getEmail())
                .orElseThrow(AuthException::invalidCredentials);

        // Kiểm tra password
        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw AuthException.invalidCredentials();
        }

        // Kiểm tra status
        if (!UserStatus.ACTIVE.getValue().equals(user.getStatus())) {
            throw AuthException.userDisabled();
        }

        // Tìm game profile
        GameProfile gameProfile = gameProfileRepository.findByUserId(user.getId())
                .orElse(null);

        // Lấy permissions của user theo role
        List<RolePermission> rolePermissions = rolePermissionRepository.findActivePermissionsByRoleId(user.getRole().getId());
        List<String> permissions = rolePermissions.stream()
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.toList());

        // Tạo JWT token (có thể thêm permissions vào payload sau)
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Map response
        LoginResponse.UserProfileResponse.RoleResponse roleResponse = new LoginResponse.UserProfileResponse.RoleResponse();
        roleResponse.setId(user.getRole().getId());
        roleResponse.setName(user.getRole().getName());

        LoginResponse.UserProfileResponse.GameProfileResponse gameProfileResponse = null;
        if (gameProfile != null) {
            gameProfileResponse = new LoginResponse.UserProfileResponse.GameProfileResponse();
            gameProfileResponse.setDisplayName(gameProfile.getDisplayName());
            gameProfileResponse.setAvatarUrl(gameProfile.getAvatarUrl());
            gameProfileResponse.setCurrentScore(gameProfile.getCurrentScore());
            gameProfileResponse.setClickPower(gameProfile.getClickPower());
            gameProfileResponse.setUpgradeLevel(gameProfile.getUpgradeLevel());
        }

        LoginResponse.UserProfileResponse userProfile = new LoginResponse.UserProfileResponse();
        userProfile.setId(user.getId());
        userProfile.setEmail(user.getEmail());
        userProfile.setRole(roleResponse);
        userProfile.setProfile(gameProfileResponse);
        userProfile.setPermissions(permissions); // Thêm permissions cho FE

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(jwtUtil.getExpirationTime());
        response.setUser(userProfile);

        log.info("Login successful for user: {} with permissions: {}", user.getEmail(), permissions);
        return response;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Attempting registration for email: {}", request.getEmail());

        // Validate input
        validateRegisterRequest(request);

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyExists();
        }

        // Tìm role USER (id=1 theo bảng phân quyền)
        Role userRole = roleRepository.findByName(RoleName.USER.getValue())
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        // Tạo user mới
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordUtil.encode(request.getPassword()));
        newUser.setRole(userRole);
        newUser.setStatus(UserStatus.ACTIVE.getValue());

        User savedUser = userRepository.save(newUser);

        // Tạo game profile mặc định
        createDefaultGameProfile(savedUser);

        log.info("Registration successful for user: {} with role: {}", savedUser.getEmail(), userRole.getName());
        return authMapper.toRegisterResponse(savedUser);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        // Không tiết lộ thông tin user có tồn tại hay không
        // Chỉ log để admin biết
        boolean userExists = userRepository.existsByEmail(request.getEmail());
        if (userExists) {
            log.info("Password reset requested for existing user: {}", request.getEmail());
            // TODO: Implement send OTP logic here
        } else {
            log.warn("Password reset requested for non-existing user: {}", request.getEmail());
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request for email: {}", request.getEmail());

        // Validate input
        validateResetPasswordRequest(request);

        // Tìm user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AuthException.validationError("Email không tồn tại"));

        // TODO: Validate OTP here (for now, we'll skip OTP validation)
        // In real implementation, you would validate the OTP against stored value

        // Update password
        user.setPassword(passwordUtil.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resyncUser(ResyncUserRequest request) {
        log.info("Resync user request for userId: {}", request.getUserId());

        // Tìm user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> AuthException.validationError("User không tồn tại"));

        // Tìm game profile
        GameProfile gameProfile = gameProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> AuthException.validationError("Game profile không tồn tại"));

        // Update game profile data
        if (request.getCurrentScore() != null) {
            gameProfile.setCurrentScore(request.getCurrentScore());
        }
        if (request.getClickPower() != null) {
            gameProfile.setClickPower(request.getClickPower());
        }
        if (request.getUpgradeLevel() != null) {
            gameProfile.setUpgradeLevel(request.getUpgradeLevel());
        }

        gameProfile.setLastActiveAt(LocalDateTime.now());
        gameProfileRepository.save(gameProfile);

        log.info("User resync successful for userId: {} with score: {}, clickPower: {}, upgradeLevel: {}",
                user.getId(), gameProfile.getCurrentScore(), gameProfile.getClickPower(), gameProfile.getUpgradeLevel());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw AuthException.validationError("Mật khẩu xác nhận không khớp");
        }

        if (!passwordUtil.isValidPassword(request.getPassword())) {
            throw AuthException.validationError("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    private void validateResetPasswordRequest(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw AuthException.validationError("Mật khẩu xác nhận không khớp");
        }

        if (!passwordUtil.isValidPassword(request.getNewPassword())) {
            throw AuthException.validationError("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    private void createDefaultGameProfile(User user) {
        GameProfile gameProfile = new GameProfile();
        gameProfile.setUser(user);
        gameProfile.setDisplayName("Player" + user.getId());
        gameProfile.setAvatarUrl(null);
        gameProfile.setCurrentScore(0L);
        gameProfile.setClickPower(1);
        gameProfile.setUpgradeLevel(1);
        gameProfile.setStatus(GameProfileStatus.ACTIVE.getValue());
        gameProfile.setLastActiveAt(LocalDateTime.now());

        gameProfileRepository.save(gameProfile);
        log.info("Default game profile created for user: {}", user.getEmail());
    }
}