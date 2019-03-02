package me.devcode.mongoapi;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import lombok.Getter;
import me.devcode.mongoapi.database.DatabaseAPI;
import me.devcode.mongoapi.stats.StatsAPI;

@Getter

public class MongoAPI extends JavaPlugin {

    private ExecutorService executorService = Executors.newCachedThreadPool();
    @Getter
    private static MongoAPI instance;
    private Gson gson = new Gson();
    private DatabaseAPI databaseAPI;
    private StatsAPI statsAPI;

    @Override
    public void onEnable() {
        instance = this;
        databaseAPI = new DatabaseAPI();

        statsAPI = StatsAPI.getInstance();
        List<String> list = new ArrayList<>();
        list.add("kills");
        list.add("deaths");
        statsAPI.setGameMode("UHC");
        statsAPI.setDefaultValues(list);
        Random ran = new Random();
        new BukkitRunnable() {

            @Override
            public void run() {

                System.out.println("Starting Randoms");
                IntStream.range(0, 1000).forEach(i -> {
                    UUID uuid = UUID.randomUUID();
                    statsAPI.register(uuid);
                    statsAPI.setValue(uuid, "kills", ran.nextInt(5) + 1);
                    System.out.println(statsAPI.getStats(uuid).getValue("kills"));
                });
                System.out.println("Finished Randoms");
            }
    }.runTaskLaterAsynchronously(this, 20);

    }
}
