package org.sacabam.sacabamclickerbe.enums.auth;

public enum RolePermissionStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String value;

    RolePermissionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RolePermissionStatus fromValue(String value) {
        for (RolePermissionStatus status : RolePermissionStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown role permission status: " + value);
    }
}