package com.phonkedit;

import com.phonkedit.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhonkEditMod implements ModInitializer {
    public static final String MOD_ID = "phonkedit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Phonk Edit");
        ModConfig.load();
        ModSounds.initialize();
    }
}
