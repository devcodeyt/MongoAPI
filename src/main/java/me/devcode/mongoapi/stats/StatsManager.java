package me.devcode.mongoapi.stats;

import java.util.List;

public class StatsManager {

    private List<String> defaultValues;
    private String gameMode;

    private static StatsManager instance;

    public static StatsManager getInstance() {
        if(instance == null)
            instance = new StatsManager();
        return instance;
    }

    public void setDefaultValues(List<String> defaultValue) {
        defaultValues = defaultValue;
    }

    public List<String> getDefaultValues() {
        return defaultValues;
    }

    public void setGameMode(String gm) {
        gameMode = gm;
    }

    public String getGameMode() {
        return gameMode;
    }
}
