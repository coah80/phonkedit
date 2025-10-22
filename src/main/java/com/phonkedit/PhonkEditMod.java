package com.phonkedit;

import com.phonkedit.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhonkEditMod implements ModInitializer {
    public static final String MOD_ID = "phonkedit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Phonk Edit");
        ModConfig.load();

        if (!FabricLoader.getInstance().isModLoaded("modmenu") && !ModConfig.INSTANCE.modMenuDisclaimerShown) {
            LOGGER.warn("Phonk Edit: Mod Menu not detected. Install Mod Menu for the full configuration UI or continue using the bundled fallback screen.");
            ModConfig.INSTANCE.modMenuDisclaimerShown = true;
            ModConfig.save();
        }
        ModSounds.initialize();
    }
}
