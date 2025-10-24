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
import net.fabricmc.loader.api.FabricLoader;
import com.phonkedit.config.ModConfig;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class CustomSongs {
    private static final Gson GSON = new Gson();
    private static volatile Identifier[] discoveredSoundIds = new Identifier[0];

    private CustomSongs() {}

    public static void initializeOnClientStart() {
        tryCreateTutorialPack();
    }

    public static void onResourceReload(ResourceManager manager) {
        scanLayeredSounds(manager);
    }

    public static SoundEvent[] getCurrentPhonkSoundEvents(SoundEvent[] builtin) {
        Identifier[] ids = discoveredSoundIds;
    boolean onlyCustom = ModConfig.INSTANCE.onlyUseCustomSongs;
        if (ids.length == 0) {
            if (onlyCustom) return new SoundEvent[0];
            return builtin;
        }
        List<SoundEvent> events = new ArrayList<>(ids.length + (onlyCustom ? 0 : builtin.length));
        for (Identifier id : ids) events.add(SoundEvent.of(id));
        if (!onlyCustom) {
            for (SoundEvent b : builtin) events.add(b);
        }
        return events.toArray(new SoundEvent[0]);
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
                        if (v != null && v.isJsonObject()) keys.add(k);
                    }
                }
            } catch (Exception ignored) { }
        }
        if (!keys.isEmpty()) {
            List<String> customKeys = new ArrayList<>();
            for (String k : keys) {
                if (k.startsWith("custom/")) customKeys.add(k);
            }
            if (!customKeys.isEmpty()) keys = new HashSet<>(customKeys);
        }
        if (keys.isEmpty()) {
            discoveredSoundIds = new Identifier[0];
            return;
        }
        List<Identifier> out = new ArrayList<>(keys.size());
        for (String k : keys) out.add(Identifier.of("phonkedit", k));
        discoveredSoundIds = out.toArray(new Identifier[0]);
        PhonkEditMod.LOGGER.info("Discovered {} phonk sound events from resource packs", discoveredSoundIds.length);
    }

    private static void tryCreateTutorialPack() {
        try {
            Path packsDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
            Path packRoot = packsDir.resolve("PhonkEdit-CustomSongs");
            Files.createDirectories(packRoot);
            String mcmeta = "{\n  \"pack\": {\n    \"pack_format\": 48,\n    \"description\": \"Phonk Edit Custom Songs Tutorial\"\n  }\n}";
            Files.writeString(packRoot.resolve("pack.mcmeta"), mcmeta, StandardCharsets.UTF_8);
            Path assetsRoot = packRoot.resolve("assets").resolve("phonkedit");
            Path soundsDir = assetsRoot.resolve("sounds");
            Files.createDirectories(soundsDir);
        String readme = "How to add custom songs (2025-10-24)\r\n\r\n" +
                    "1) Create your own resource pack (zip or folder inside resourcepacks).\r\n" +
                    "2) Inside it, create these paths:\r\n" +
                    "   assets/phonkedit/sounds.json\r\n \r\n" +
                    "3) Put your .ogg files in assets/phonkedit/sounds/.\r\n" +
                    "   Example: assets/phonkedit/sounds/custom/bruh.ogg\r\n\r\n" +
                    "4) Edit assets/phonkedit/sounds.json to reference them. Example:\r\n\r\n" +
                    "{\r\n" +
                    "  \"custom/bruh\": { \"category\": \"master\", \"sounds\": [ \"phonkedit:custom/bruh\" ] },\r\n" +
                    "  \"custom/hahahafunisong\": { \"category\": \"master\", \"sounds\": [ \"phonkedit:custom/hahahafunisong\" ] }\r\n" +
                    "}\r\n\r\n" +
                    "5) Enable your pack in Options -> Resource Packs.\r\n" +
                    "6) Trigger the Phonk effect; the mod prefers keys starting with custom/.\r\n\r\n" +
                    "Tips:\r\n" +
            "- Use an audio editor like Audacity, FL Studio, or Adobe Audition to export .ogg files.\r\n" +
                    "- Names in sounds.json map to files under assets/phonkedit/sounds/.\r\n" +
                    "- Use stream: true in your entries for long tracks.\r\n" +
                    "- Keep filenames lowercase and avoid spaces.\r\n";
            Files.writeString(soundsDir.resolve("HOW TO ADD CUSTOM SONGS.txt"), readme, StandardCharsets.UTF_8);
            Path exampleJson = assetsRoot.resolve("sounds.json");
            if (!Files.exists(exampleJson)) {
                String example = "{\n" +
                        "  \"custom/bruh\": { \"category\": \"master\", \"sounds\": [ \"phonkedit:custom/bruh\" ] },\n" +
                        "  \"custom/hahahafunisong\": { \"category\": \"master\", \"sounds\": [ \"phonkedit:custom/hahahafunisong\" ] }\n" +
                        "}\n";
                Files.writeString(exampleJson, example, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            PhonkEditMod.LOGGER.debug("Failed to create tutorial pack: {}", e.toString());
        }
    }
}
