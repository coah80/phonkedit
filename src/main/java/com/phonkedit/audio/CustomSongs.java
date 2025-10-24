package com.phonkedit.audio;

import com.phonkedit.PhonkEditMod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;

public final class CustomSongs {
    private static final Path SONGS_DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("phonkedit").resolve("songs");

    private CustomSongs() {
    }

    public static void prepareAndReload(ResourceManager manager) {
        try {
            Files.createDirectories(SONGS_DIRECTORY);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Failed to prepare custom songs directory", e);
        }
    }
}
