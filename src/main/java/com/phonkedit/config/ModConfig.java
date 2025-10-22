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

    // Additional triggers
    public boolean triggerOnBlockBreak = true;
    public boolean triggerOnEntityHit = true;
    public boolean triggerOnDamageTaken = true;
    public boolean triggerOnLowHealth = true;
    public float lowHealthThreshold = 6.0f; // 3 hearts

    // Safety/"pause" options
    public boolean lockMouseDuringEffect = true;
    public boolean lockCameraDuringEffect = true;
    public boolean pauseServerDuringEffect = true;

    // Audio tempo range
    public double phonkPitchMin = 0.95;
    public double phonkPitchMax = 1.05;

    // Visual options
    public boolean grayscaleFreezeFrame = true;
    public boolean darkenScreenDuringEffect = true;
    public boolean showCinematicBars = true;
    public boolean renderSkullOverlay = true;
    public boolean skullShakeEnabled = true;
    public boolean skullBlurEnabled = true;

    // Misc state
    public boolean modMenuDisclaimerShown = false;

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
        INSTANCE.normalize();
        save();
    }

    public static void save() {
        try {
            INSTANCE.normalize();
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Failed to save config", e);
        }
    }

    private void normalize() {
        double min = Math.min(phonkPitchMin, phonkPitchMax);
        double max = Math.max(phonkPitchMin, phonkPitchMax);
        min = clampPitch(min);
        max = clampPitch(max);

        phonkPitchMin = min;
        phonkPitchMax = max;
    }

    private static double clampPitch(double value) {
        return Math.max(0.5, Math.min(2.0, value));
    }
}
