package org.insider.util;

public enum Region {
    GB("GB"),
    US("US");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
