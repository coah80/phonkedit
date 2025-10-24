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
    public double triggerChance = 0.30;
    public int effectDuration = 3000;
    public double shakeIntensity = 1.0;
    public double phonkPitchMin = 0.95;
    public double phonkPitchMax = 1.05;
    public boolean mixBuiltinWithCustomSongs = true;
    public boolean grayscaleFreezeFrame = true;
    public boolean darkenScreenDuringEffect = true;
    public boolean showCinematicBars = true;
    public boolean renderSkullOverlay = true;
    public double skullScale = 0.4;
    public boolean skullShakeEnabled = true;
    public boolean skullBlurEnabled = true;
    public double skullBlurIntensity = 5.0;
    public double skullBlurEasePower = 1.5;
    public double airTimeThresholdSeconds = 1.3;
    public float lowHealthThreshold = 6.0f;
    public boolean triggerOnBlockBreak = true;
    public boolean triggerOnBlockPlace = true;
    public boolean triggerOnEntityHit = true;
    public boolean triggerOnDamageTaken = true;
    public boolean triggerOnLowHealth = true;
    public boolean triggerOnAirTime = true;
    public boolean triggerOnLeverUse = true;
    public boolean triggerOnDoorUse = true;
    public boolean triggerOnVehicleMount = true;
    public boolean triggerOnEatFood = true;
    public boolean triggerOnUseBed = true;
    public boolean triggerOnDragonKill = true;
    public boolean triggerOnWitherKill = true;
    public boolean triggerOnWardenKill = true;
    public boolean triggerOnElderGuardianKill = true;
    public boolean pauseServerDuringEffect = true;
    public boolean lockMouseDuringEffect = true;
    public boolean lockCameraDuringEffect = true;
    public boolean devDisablePauseOnLostFocus = false;
    public boolean devDontEndOnPause = false;
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
