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
    private MongoCollection<Document> playerCollection;
    private File file = new File("plugins/MongoAPI", "settings.yml");

    public DatabaseAPI() {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.options().copyDefaults(true);
        cfg.addDefault("Host", "host");
        cfg.addDefault("User", "user");
        cfg.addDefault("Port", 27017);
        cfg.addDefault("Password", "password");
        cfg.addDefault("Database", "database");
        cfg.addDefault("Collections", "collections");

        try {
            cfg.save(file);
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        String host = cfg.getString("Host");
        String user = cfg.getString("User");
        String db = cfg.getString("Database");
        int port = cfg.getInt("Port");
        char[] password = cfg.getString("Password").toCharArray();
        MongoCredential mongoCredential = MongoCredential.createCredential(user, user, password);
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        MongoDriverInformation mongoDriverInformation = MongoDriverInformation.builder().build();
        //mongoClient =  new MongoClient(new ServerAddress(host, port), mongoCredential, mongoClientOptions, mongoDriverInformation);
        mongoClient =  new MongoClient(new ServerAddress(host, port));
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
