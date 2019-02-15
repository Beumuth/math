package com.beumuth.collections.client.application;

public enum ApplicationMode {
    LIVE,
    TEST;

    public static boolean doesApplicationModeExistByName(String name) {
        for(ApplicationMode mode : ApplicationMode.values()) {
            if(mode.name().toUpperCase().equals(name.toUpperCase())) {
                return true;
            }
        }

        return false;
    }
}
