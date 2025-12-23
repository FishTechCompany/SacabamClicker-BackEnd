package org.sacabam.sacabamclickerbe.dto.request.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResyncUserRequest {

    @NotNull(message = "User ID không được để trống")
    private Integer userId;

    private Long currentScore;
    private Integer clickPower;
    private Integer upgradeLevel;
}