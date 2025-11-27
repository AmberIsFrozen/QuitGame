package com.lx862.quitgame.config;

import com.google.gson.*;
import com.lx862.quitgame.QuitGame;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static Path configFile = FabricLoader.getInstance().getConfigDir().resolve("quitgame").resolve("config.json");

    public static void load() {
        try {
            Files.createDirectories(configFile.getParent());

            if(!Files.exists(configFile)) {
                QuitGame.LOGGER.info("[QuitGame] Config not found, generating one...");
                write();
                load();
            } else {
                JsonElement jsonElement = JsonParser.parseReader(new FileReader(configFile.toFile()));
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if(jsonObject.get("alwaysUnlock").getAsBoolean()) {
                    QuitGame.unlocked = true;
                }

                JsonArray keywords = jsonObject.get("keywords").getAsJsonArray();
                for(int i = 0; i < keywords.size(); i++) {
                    QuitGame.keywords.add(keywords.get(i).getAsString());
                }
            }
        } catch (Exception e) {
            QuitGame.LOGGER.error("", e);
        }
    }

    public static void write() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("alwaysUnlock", false);

        JsonArray keywords = new JsonArray();
        keywords.add("init");
        keywords.add("start");
        keywords.add("menu");
        keywords.add("play");
        keywords.add("poison");
        jsonObject.add("keywords", keywords);

        try (Writer writer = new FileWriter(configFile.toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        } catch (Exception e) {
            QuitGame.LOGGER.error("", e);
        }
    }
}
