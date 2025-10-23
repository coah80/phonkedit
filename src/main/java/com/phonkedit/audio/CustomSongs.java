package com.phonkedit.audio;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phonkedit.PhonkEditMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CustomSongs {
    private static final Gson GSON = new Gson();
    private static final List<SoundEvent> CUSTOM_SONG_EVENTS = new ArrayList<>();

    private static final Path USER_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("phonkedit");
    private static final Path USER_SONGS_DIR = USER_CONFIG_DIR.resolve("songs");
    private static final Path GAME_RESOURCEPACKS_DIR = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private static final Path GENERATED_PACK_DIR = GAME_RESOURCEPACKS_DIR.resolve("phonkedit-custom-songs");
    private static final String SONGS_MARKER = ".default_songs_copied";

    private CustomSongs() {}

    public static void prepareAndReload(ResourceManager resourceManager) {
        ensureDirs();
        exportDefaultSongsIfNeeded(resourceManager);
        try {
            generateOrUpdateResourcePack();
        } catch (IOException e) {
            PhonkEditMod.LOGGER.warn("Failed to generate/update custom songs resource pack", e);
        }
        reload(resourceManager);
    }

    private static void ensureDirs() {
        try {
            Files.createDirectories(USER_SONGS_DIR);
            Files.createDirectories(GENERATED_PACK_DIR);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Failed to prepare directories for custom songs", e);
        }
    }

    private static void exportDefaultSongsIfNeeded(ResourceManager resourceManager) {
        Path marker = USER_SONGS_DIR.resolve(SONGS_MARKER);
        if (Files.exists(marker)) return;

        String[] names = new String[] {
                "phonk1.ogg","phonk2.ogg","phonk3.ogg","phonk4.ogg","phonk5.ogg","phonk6.ogg",
                "phonk7.ogg","phonk8.ogg","phonk9.ogg","phonk10.ogg","phonk11.ogg","phonk12.ogg"
        };
        for (String file : names) {
            Identifier src = Identifier.of("phonkedit", "phonk/" + file);
            try {
                Resource res = resourceManager.getResource(src).orElse(null);
                if (res == null) continue;
                try (InputStream in = res.getInputStream()) {
                    Path target = USER_SONGS_DIR.resolve(file);
                    if (!Files.exists(target)) {
                        Files.copy(in, target);
                    }
                }
            } catch (Exception e) {
                PhonkEditMod.LOGGER.warn("Failed to export built-in song {}", file, e);
            }
        }
        try {
            Files.writeString(marker, "ok");
        } catch (IOException e) {
            // non-fatal
        }
    }

    private static void generateOrUpdateResourcePack() throws IOException {
        Path packMeta = GENERATED_PACK_DIR.resolve("pack.mcmeta");
        Path assetsDir = GENERATED_PACK_DIR.resolve("assets").resolve("phonkedit");
        Path customSoundsDir = assetsDir.resolve("sounds").resolve("custom");
        Path soundsJson = assetsDir.resolve("sounds.json");

        Files.createDirectories(customSoundsDir);

        try (var stream = Files.list(customSoundsDir)) {
            stream.forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            });
        }

        List<String> resourceKeys = new ArrayList<>();
        try (var stream = Files.list(USER_SONGS_DIR)) {
            stream.filter(p -> {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return name.endsWith(".ogg");
            }).forEach(p -> {
                String base = p.getFileName().toString();
                String baseNoExt = base.substring(0, base.length() - 4);
                String sanitized = baseNoExt.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_");
                if (sanitized.isBlank()) return;
                Path target = customSoundsDir.resolve(sanitized + ".ogg");
                try {
                    Files.copy(p, target);
                    resourceKeys.add("custom/" + sanitized);
                } catch (IOException e) {
                    PhonkEditMod.LOGGER.warn("Failed copying song {} to generated pack", p, e);
                }
            });
        }

        JsonObject root = new JsonObject();
        for (String key : resourceKeys) {
            JsonObject def = new JsonObject();
            var arr = new com.google.gson.JsonArray();
            JsonObject sound = new JsonObject();
            sound.addProperty("name", "phonkedit:" + key);
            sound.addProperty("stream", true);
            arr.add(sound);
            def.add("sounds", arr);
            root.add(key, def);
        }
        try (BufferedWriter w = Files.newBufferedWriter(soundsJson, StandardCharsets.UTF_8)) {
            w.write(GSON.toJson(root));
        }

        String meta = "{\n  \"pack\": {\n    \"pack_format\": 32,\n    \"description\": \"Phonk Edit - Custom Songs (generated)\"\n  }\n}\n";
        Files.writeString(packMeta, meta, StandardCharsets.UTF_8);

        if (!resourceKeys.isEmpty()) {
            PhonkEditMod.LOGGER.info("Generated/updated resource pack '{}' with {} song(s). Enable it in Resource Packs menu.", GENERATED_PACK_DIR.getFileName(), resourceKeys.size());
        }
    }

    private static void reload(ResourceManager resourceManager) {
        CUSTOM_SONG_EVENTS.clear();
        try {
            Identifier soundsJsonId = Identifier.of("phonkedit", "sounds.json");
            Resource res = resourceManager.getResource(soundsJsonId).orElse(null);
            if (res == null) {
                PhonkEditMod.LOGGER.debug("No phonkedit sounds.json found for custom songs");
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) return;
                for (String key : root.keySet()) {
                    if (!key.startsWith("custom/")) continue;
                    JsonElement elem = root.get(key);
                    if (elem == null || !elem.isJsonObject()) continue;
                    Identifier id = Identifier.of("phonkedit", key);
                    SoundEvent event = SoundEvent.of(id);
                    CUSTOM_SONG_EVENTS.add(event);
                }
            }
            if (!CUSTOM_SONG_EVENTS.isEmpty()) {
                PhonkEditMod.LOGGER.info("Discovered {} custom phonk song(s)", CUSTOM_SONG_EVENTS.size());
            }
        } catch (Exception e) {
            PhonkEditMod.LOGGER.warn("Failed to scan custom songs from sounds.json", e);
        }
    }

    public static List<SoundEvent> getCustomSongEvents() {
        return Collections.unmodifiableList(CUSTOM_SONG_EVENTS);
    }

    public static boolean isCustom(SoundEvent event) {
        return CUSTOM_SONG_EVENTS.contains(event);
    }
}
