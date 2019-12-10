package me.devcode.mongoapi.stats;

import org.bson.Document;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class Stats {
    private UUID uuid;
    private String gameMode;
    private Map<String, Object> values = new ConcurrentHashMap<>();

    public Stats(UUID uuid, String gameMode) {
        this.uuid = uuid;
        this.gameMode = gameMode;
    }

    public void removeValue(String value) {
        this.values.remove(value);
    }

    public Object getValue(String key) {
        return values.getOrDefault(key, 0);
    }

    public void setValue(String key, Object value) {
        if (this.values.containsKey(key)) {
            this.values.replace(key, value);
        } else {
            this.values.put(key, value);
        }
    }

    public Document update() {
        Document document = (new Document("uniqueId", this.uuid.toString())).append("gamemode", this.gameMode);
        Iterator values = this.values.keySet().iterator();
        values.forEachRemaining(v ->{
            String all = (String)values.next();
            document.append(all, this.values.get(all));
        });

        return document;
    }

}
