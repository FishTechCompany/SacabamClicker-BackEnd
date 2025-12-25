package org.sacabam.sacabamclickerbe.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResyncUserRequest {

    @NotBlank(message = "Token không được để trống")
    private String token;

    private Long currentScore;
    private Integer clickPower;
    private Integer upgradeLevel;
}