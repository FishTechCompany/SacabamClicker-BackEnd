package org.sacabam.sacabamclickerbe.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Integer expiresIn;
    private UserProfileResponse user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileResponse {
        private Integer id;
        private String email;
        private RoleResponse role;
        private GameProfileResponse profile;
        private List<String> permissions; // Thêm permissions cho FE render UI

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RoleResponse {
            private Integer id;
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class GameProfileResponse {
            private String displayName;
            private String avatarUrl;
            private Long currentScore;
            private Integer clickPower;
            private Integer upgradeLevel;
        }
    }
}