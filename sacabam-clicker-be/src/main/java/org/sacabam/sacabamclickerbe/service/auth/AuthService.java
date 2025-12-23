package org.sacabam.sacabamclickerbe.service.auth;

import org.sacabam.sacabamclickerbe.dto.request.auth.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.auth.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.auth.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void resyncUser(ResyncUserRequest request);
}