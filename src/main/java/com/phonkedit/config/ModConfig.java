package com.phonkedit.config;

import com.phonkedit.PhonkEditMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("phonkedit.json");

    public boolean enablePhonkEffect = true;
    public double triggerChance = 0.10;
    public int effectDuration = 3000;
    public double shakeIntensity = 1.0; // Multiplier for shake effect (1.0 = normal, higher = more intense)

    public static ModConfig INSTANCE = new ModConfig();

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
            } catch (IOException e) {
                PhonkEditMod.LOGGER.error("Failed to load config", e);
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Failed to save config", e);
        }
    }
}
