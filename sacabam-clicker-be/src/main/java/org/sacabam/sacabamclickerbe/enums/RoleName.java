package org.sacabam.sacabamclickerbe.enums;

public enum RoleName {
    USER("USER"),           // id=1
    PRO("PRO"),            // id=2
    ADMIN("ADMIN");        // id=0

    private final String value;

    RoleName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleName fromValue(String value) {
        for (RoleName role : RoleName.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}