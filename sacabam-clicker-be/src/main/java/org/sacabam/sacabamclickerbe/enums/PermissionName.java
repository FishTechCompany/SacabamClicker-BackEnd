package org.sacabam.sacabamclickerbe.enums;

public enum PermissionName {
    PLAY_GAME("PLAY_GAME"),                 // id=0 - Chơi và sync điểm
    SWITCH_THEME("SWITCH_THEME"),           // id=1 - Đổi giao diện
    VIEW_LEADERBOARD("VIEW_LEADERBOARD"),   // id=2 - Xem BXH
    DISCOUNT_TIER_1("DISCOUNT_TIER_1"),     // id=3 - Giảm giá cấp 1
    MANAGE_USERS("MANAGE_USERS"),           // id=4 - Quản lý User
    ROLE_MANAGER("ROLE_MANAGER"),           // id=5 - Quản lý Role
    CHEAT_PERMISSION("CHEAT_PERMISSION");   // id=6 - Menu Cheat

    private final String value;

    PermissionName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PermissionName fromValue(String value) {
        for (PermissionName permission : PermissionName.values()) {
            if (permission.value.equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + value);
    }
}