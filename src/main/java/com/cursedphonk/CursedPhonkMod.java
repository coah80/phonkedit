package com.cursedphonk;

import com.cursedphonk.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CursedPhonkMod implements ModInitializer {
    public static final String MOD_ID = "cursedphonk-renderer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing CursedPhonk Renderer Edition");
        ModConfig.load();
        ModSounds.initialize();
    }
}
