package me.devcode.mongoapi.stats;

import org.bson.Document;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Stats {

    private UUID player;
    private String gameMode;
    private Map<String, Object> values = new ConcurrentHashMap<>();

    public Stats(UUID player, String gameMode) {
        this.player = player;
        this.gameMode = gameMode;
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public String getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public void addValue(String value) {
        this.values.put(value, 0);
    }

    public void removeValue(String value) {
        this.values.remove(value);
    }

    public void setValue(String key, Object value) {
        if (this.values.containsKey(key)) {
            this.values.replace(key, value);
        } else {
            this.values.put(key, value);
        }

    }

    public Object getValue(String key) {
        return this.values.get(key);
    }

    public Document update() {
        Document document = (new Document("uniqueId", this.player.toString())).append("gamemode", this.gameMode);
        Iterator values = this.values.keySet().iterator();
        values.forEachRemaining(v ->{
            String all = (String)values.next();
            document.append(all, this.values.get(all));
        });

        return document;
    }

    public UUID getPlayer() {
        return this.player;
    }

}
