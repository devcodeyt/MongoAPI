package me.devcode.mongoapi;

import com.google.gson.Gson;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import me.devcode.mongoapi.database.DatabaseAPI;

@Getter

public class MongoAPI extends JavaPlugin {

    private ExecutorService executorService = Executors.newCachedThreadPool();
    @Getter
    private static MongoAPI instance;
    private Gson gson = new Gson();
    private DatabaseAPI databaseAPI;

    @Override
    public void onEnable() {
        instance = this;
        databaseAPI = new DatabaseAPI();

    }
}
