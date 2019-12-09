package me.devcode.mongoapi.database;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import lombok.Getter;

@Getter
public class DatabaseAPI {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
   // private MongoCollection<Document> playerCollection;
    private File file = new File("plugins/mongoapi", "settings.yml");

    public DatabaseAPI() {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.options().copyDefaults(true);
        cfg.addDefault("host", "host");
        cfg.addDefault("user", "user");
        cfg.addDefault("port", 27017);
        cfg.addDefault("password", "password");
        cfg.addDefault("databse", "database");
        cfg.addDefault("collections", "collections");

        try {
            cfg.save(file);
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        String host = cfg.getString("host");
        String user = cfg.getString("user");
        String db = cfg.getString("database");
        int port = cfg.getInt("port");
        char[] password = cfg.getString("password").toCharArray();
        MongoCredential mongoCredential = MongoCredential.createCredential(user, user, password);
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        MongoDriverInformation mongoDriverInformation = MongoDriverInformation.builder().build();
        mongoClient =  new MongoClient(new ServerAddress(host, port), mongoCredential, mongoClientOptions, mongoDriverInformation);
        mongoDatabase = this.mongoClient.getDatabase(db);
        if(mongoClient == null) {
            System.out.println("MongoDB couldnt connect.");
            return;
        }
    }

    public MongoCollection<Document> getCustomCollection(String name) {
        return this.mongoDatabase.getCollection(name);
    }

}
