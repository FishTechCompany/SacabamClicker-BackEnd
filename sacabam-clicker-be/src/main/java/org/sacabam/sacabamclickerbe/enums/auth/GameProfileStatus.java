package org.sacabam.sacabamclickerbe.enums.auth;

public enum GameProfileStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    SUSPENDED("SUSPENDED");

    private final String value;

    GameProfileStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GameProfileStatus fromValue(String value) {
        for (GameProfileStatus status : GameProfileStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown game profile status: " + value);
    }
}