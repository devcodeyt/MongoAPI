package me.devcode.mongoapi.stats;

import java.util.List;

public class StatsManager {

    private static List<String> defaultValues;
    private static String gameMode;

    public StatsManager() {
    }

    public static void setDefaultValues(List<String> defaultValue) {
        defaultValues = defaultValue;
    }

    public static List<String> getDefaultValues() {
        return defaultValues;
    }

    public static void setGameMode(String gm) {
        gameMode = gm;
    }

    public static String getGameMode() {
        return gameMode;
    }
}
