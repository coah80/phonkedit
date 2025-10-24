package com.phonkedit.audio;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phonkedit.PhonkEditMod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class CustomSongs {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("phonkedit");
    private static final Path SONGS_DIR = CONFIG_DIR.resolve("songs");
    private static final String PACK_NAME = "PhonkEdit-UserMedia";
    private static final Gson GSON = new Gson();
    private static volatile Identifier[] discoveredSoundIds = new Identifier[0];
    private static volatile boolean started = false;

    private CustomSongs() {
    }

    public static void initializeOnClientStart() {
        if (started) return;
        started = true;
        try {
            Files.createDirectories(SONGS_DIR);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.warn("Could not create songs config directory: {}", e.toString());
        }
        tryBuildUserPack();
        try {
            MinecraftClient.getInstance().reloadResources();
        } catch (Throwable t) {
            PhonkEditMod.LOGGER.debug("Resource reload request failed: {}", t.toString());
        }
    }

    public static void onResourceReload(ResourceManager manager) {
        tryBuildUserPack();
        scanLayeredSounds(manager);
    }

    public static SoundEvent[] getCurrentPhonkSoundEvents(SoundEvent[] builtin) {
        Identifier[] ids = discoveredSoundIds;
        if (ids.length == 0) {
            return builtin;
        }
        List<SoundEvent> events = new ArrayList<>(ids.length);
        for (Identifier id : ids) {
            events.add(SoundEvent.of(id));
        }
        return events.toArray(new SoundEvent[0]);
    }

    private static void tryBuildUserPack() {
        try {
            Path packsDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
            Path packRoot = packsDir.resolve(PACK_NAME);
            Path packAssets = packRoot.resolve("assets").resolve("phonkedit");
            Files.createDirectories(packAssets);
            Path mcmeta = packRoot.resolve("pack.mcmeta");
            String meta = "{\n  \"pack\": {\n    \"pack_format\": 48,\n    \"description\": \"Phonk Edit User Media\"\n  }\n}";
            Files.writeString(mcmeta, meta, StandardCharsets.UTF_8);
            Path srcJson = SONGS_DIR.resolve("sounds.json");
            if (Files.isRegularFile(srcJson)) {
                Path dstJson = packAssets.resolve("sounds.json");
                Files.createDirectories(dstJson.getParent());
                Files.copy(srcJson, dstJson, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            Path srcSounds = SONGS_DIR.resolve("sounds");
            if (Files.isDirectory(srcSounds)) {
                Path dstSounds = packAssets.resolve("sounds");
                copyRecursive(srcSounds, dstSounds);
            }
        } catch (IOException e) {
            PhonkEditMod.LOGGER.warn("Failed to build user media pack: {}", e.toString());
        }
    }

    private static void copyRecursive(Path src, Path dst) throws IOException {
        if (!Files.exists(src)) return;
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(path -> {
                try {
                    Path rel = src.relativize(path);
                    Path out = dst.resolve(rel);
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(out);
                    } else {
                        Files.createDirectories(out.getParent());
                        Files.copy(path, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    PhonkEditMod.LOGGER.debug("Copy failed: {}", e.toString());
                }
            });
        }
    }

    private static void scanLayeredSounds(ResourceManager manager) {
        Set<String> keys = new HashSet<>();
        List<Resource> resources = manager.getAllResources(Identifier.of("phonkedit", "sounds.json"));
        for (Resource res : resources) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null) {
                    for (String k : obj.keySet()) {
                        JsonElement v = obj.get(k);
                        if (v != null && v.isJsonObject()) {
                            keys.add(k);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (!keys.isEmpty()) {
            List<String> songKeys = new ArrayList<>();
            for (String k : keys) {
                if (k.startsWith("song")) {
                    int i = 4;
                    boolean ok = i < k.length();
                    for (; ok && i < k.length(); i++) {
                        char c = k.charAt(i);
                        if (c < '0' || c > '9') {
                            ok = false;
                        }
                    }
                    if (ok) songKeys.add(k);
                }
            }
            if (!songKeys.isEmpty()) {
                keys = new HashSet<>(songKeys);
            }
        }
        if (keys.isEmpty()) {
            discoveredSoundIds = new Identifier[0];
            return;
        }
        List<Identifier> out = new ArrayList<>(keys.size());
        for (String k : keys) {
            out.add(Identifier.of("phonkedit", k));
        }
        discoveredSoundIds = out.toArray(new Identifier[0]);
        PhonkEditMod.LOGGER.info("Discovered {} phonk sound events from resource packs", discoveredSoundIds.length);
    }
}
