package org.sacabam.sacabamclickerbe.service;

import org.sacabam.sacabamclickerbe.dto.request.ForgotPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.LoginRequest;
import org.sacabam.sacabamclickerbe.dto.request.RegisterRequest;
import org.sacabam.sacabamclickerbe.dto.request.ResetPasswordRequest;
import org.sacabam.sacabamclickerbe.dto.request.ResyncUserRequest;
import org.sacabam.sacabamclickerbe.dto.response.LoginResponse;
import org.sacabam.sacabamclickerbe.dto.response.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void resyncUser(ResyncUserRequest request);
}