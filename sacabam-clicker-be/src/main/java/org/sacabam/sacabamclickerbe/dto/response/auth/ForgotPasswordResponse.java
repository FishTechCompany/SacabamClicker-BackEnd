package org.sacabam.sacabamclickerbe.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String email;
    private String message;
    private Integer otpExpirationMinutes;
}