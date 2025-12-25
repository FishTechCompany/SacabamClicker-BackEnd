package org.sacabam.sacabamclickerbe.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResyncUserResponse {
    private Integer userId;
    private String email;
    private Long currentScore;
    private Integer clickPower;
    private Integer upgradeLevel;
    private String message;
}