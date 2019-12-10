package me.devcode.mongoapi.stats;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import me.devcode.mongoapi.MongoAPI;

public class StatsAPI {

    private static StatsAPI instance;
    private List<Stats> cachedStats = new ArrayList();

    public static StatsAPI getInstance() {
        if (instance == null)
            instance = new StatsAPI();

        return instance;
    }

    public void setDefaultValues(List<String> values) {
        StatsManager.getInstance().setDefaultValues(values);
    }

    public void setGameMode(String gameMode) {
        StatsManager.getInstance().setGameMode(gameMode);
    }

    public void register(UUID uuid) {
        MongoCollection<Document> statsCollection = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats");
        if (statsCollection.find(Filters.and(new Bson[]{Filters.eq("uniqueId", uuid.toString()), Filters.eq("gamemode", StatsManager.getInstance().getGameMode())})).first() == null) {
            Stats stats = new Stats(uuid, StatsManager.getInstance().getGameMode());
            Iterator defaultValues = StatsManager.getInstance().getDefaultValues().iterator();

            while (defaultValues.hasNext()) {
                String all = (String) defaultValues.next();
                stats.setValue(all, null);
            }

            Document document = stats.update();
            statsCollection.insertOne(document);
            this.cachedStats.add(stats);
        } else {
            this.loadStats(uuid);
        }

    }

    private void loadStats(UUID uuid) {
        Document document = (Document) MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.and(new Bson[]{Filters.eq("uniqueId", uuid.toString()), Filters.eq("gamemode", StatsManager.getInstance().getGameMode())})).first();
        Stats stats = new Stats(uuid, StatsManager.getInstance().getGameMode());
        Iterator defaultValues = StatsManager.getInstance().getDefaultValues().iterator();

        while (defaultValues.hasNext()) {
            String all = (String) defaultValues.next();
            stats.setValue(all, document.getInteger(all));
        }

        this.cachedStats.add(stats);
    }

    public boolean isRegistered(UUID uuid) {
        return MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.and(new Bson[]{Filters.eq("uniqueId", uuid.toString()), Filters.eq("gamemode", StatsManager.getInstance().getGameMode())})).first() != null;
    }

    public void setValue(UUID uuid, String key, Object value) {
        Stats stats = this.getStats(uuid);
        if (stats != null) {
            stats.setValue(key, value);
        }

        MongoAPI.getInstance().getExecutorService().execute(() -> {
            MongoCollection<Document> statsCollection = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats");
            Document document = (Document) statsCollection.find(Filters.and(new Bson[]{Filters.eq("uniqueId", uuid.toString()), Filters.eq("gamemode", StatsManager.getInstance().getGameMode())})).first();
            document.replace(key, value);
            MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").replaceOne(Filters.and(new Bson[]{Filters.eq("uniqueId", uuid.toString()), Filters.eq("gamemode", StatsManager.getInstance().getGameMode())}), document);
        });
    }

    public Object getValue(UUID uuid, String value) {
        Stats stats = this.getStats(uuid);
        if (stats != null) {
            return stats.getValue(value);
        }
        this.loadStats(uuid);
        return this.getStats(uuid).getValue(value);
    }


    public int getRank(UUID uuid, String filter) {
        Future future = MongoAPI.getInstance().getExecutorService().submit(() -> {
            return this.getFutureRank(uuid, filter);
        });

        try {
            return (Integer) future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }

        return -1;
    }

    private int getFutureRank(UUID uuid, String filter) {
        FindIterable<Document> statsIterable = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.eq("gamemode", StatsManager.getInstance().getGameMode())).sort(Sorts.descending(new String[]{filter}));
        int count = 0;
        MongoCursor mongoCursor = statsIterable.iterator();

        while (mongoCursor.hasNext()) {
            Document all = (Document) mongoCursor.next();
            count++;
            if (all.getString("uniqueId").equalsIgnoreCase(uuid.toString())) {
                break;
            }
        }

        return count;
    }

    public Stats getStats(UUID uuid) {
        Iterator stats = this.cachedStats.iterator();
        Stats all;
        do {
            if (!stats.hasNext()) {
                return null;
            }

            all = (Stats) stats.next();
        } while (!all.getUuid().equals(uuid));

        return all;
    }

    public void unload(UUID uuid) {
        Stats stats = this.getStats(uuid);
        if (stats != null) {
            this.cachedStats.remove(stats);
        }

    }

}
