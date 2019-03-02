package me.devcode.mongoapi.stats;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bson.Document;
import org.bson.conversions.Bson;

import me.devcode.mongoapi.MongoAPI;

public class StatsAPI {

    private static StatsAPI instance;
    private List<Stats> cachedStats = new ArrayList();

    private StatsAPI() {
    }

    public static StatsAPI getInstance() {
        if (instance == null) {
            instance = new StatsAPI();
        }

        return instance;
    }

    public void setDefaultValues(List<String> values) {
        StatsManager.setDefaultValues(values);
    }

    public void setGameMode(String gameMode) {
        StatsManager.setGameMode(gameMode);
    }

    public void register(UUID player) {
        MongoCollection<Document> statsCollection = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats");
        if (statsCollection.find(Filters.and(new Bson[]{Filters.eq("uniqueId", player.toString()), Filters.eq("gamemode", StatsManager.getGameMode())})).first() == null) {
            Stats stats = new Stats(player, StatsManager.getGameMode());
            Iterator defaultValues = StatsManager.getDefaultValues().iterator();

            while(defaultValues.hasNext()) {
                String all = (String)defaultValues.next();
                stats.addValue(all);
            }

            Document document = stats.update();
            statsCollection.insertOne(document);
            this.cachedStats.add(stats);
        } else {
            this.loadStats(player);
        }

    }

    private void loadStats(UUID player) {
        Document document = (Document)MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.and(new Bson[]{Filters.eq("uniqueId", player.toString()), Filters.eq("gamemode", StatsManager.getGameMode())})).first();
        Stats stats = new Stats(player, StatsManager.getGameMode());
        Iterator defaultValues = StatsManager.getDefaultValues().iterator();

        while(defaultValues.hasNext()) {
            String all = (String)defaultValues.next();
            stats.setValue(all, document.getInteger(all));
        }

        this.cachedStats.add(stats);
    }

    public boolean isRegistered(UUID player) {
        return MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.and(new Bson[]{Filters.eq("uniqueId", player.toString()), Filters.eq("gamemode", StatsManager.getGameMode())})).first() != null;
    }

    public void setValue(UUID player, String key, Object value) {
        Stats stats = this.getStats(player);
        if (stats != null) {
            stats.setValue(key, value);
        }

        MongoAPI.getInstance().getExecutorService().execute(() -> {
            MongoCollection<Document> statsCollection = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats");
            Document document = (Document)statsCollection.find(Filters.and(new Bson[]{Filters.eq("uniqueId", player.toString()), Filters.eq("gamemode", StatsManager.getGameMode())})).first();
            document.replace(key, value);
            MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").replaceOne(Filters.and(new Bson[]{Filters.eq("uniqueId", player.toString()), Filters.eq("gamemode", StatsManager.getGameMode())}), document);
        });
    }

    public Object getValue(UUID player, String value) {
        Stats stats = this.getStats(player);
        if (stats != null) {
            return stats.getValue(value);
        } else {
            this.loadStats(player);
            return this.getStats(player).getValue(value);
        }
    }


    public int getRank(UUID player, String filter) {
        Future future = MongoAPI.getInstance().getExecutorService().submit(() -> {
            return this.getFutureRank(player, filter);
        });

        try {
            return (Integer)future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }

        return -1;
    }

    private int getFutureRank(UUID player, String filter) {
        FindIterable<Document> statsIterable = MongoAPI.getInstance().getDatabaseAPI().getCustomCollection("stats").find(Filters.eq("gamemode", StatsManager.getGameMode())).sort(Sorts.descending(new String[]{filter}));
        int count = 0;
        MongoCursor mongoCursor = statsIterable.iterator();

        while(mongoCursor.hasNext()) {
            Document all = (Document)mongoCursor.next();
            ++count;
            if (all.getString("uniqueId").equalsIgnoreCase(player.toString())) {
                break;
            }
        }

        return count;
    }

    public Stats getStats(UUID player) {
        Iterator stats = this.cachedStats.iterator();
        Stats all;
        do {
            if (!stats.hasNext()) {
                return null;
            }

            all = (Stats)stats.next();
        } while(!all.getPlayer().equals(player));

        return all;
    }

    public void unload(UUID player) {
        Stats stats = this.getStats(player);
        if (stats != null) {
            this.cachedStats.remove(stats);
        }

    }

}
