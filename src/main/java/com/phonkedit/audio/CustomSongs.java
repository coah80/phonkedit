package com.phonkedit.audio;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phonkedit.PhonkEditMod;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class CustomSongs {
    private static final Gson GSON = new Gson();
    private static volatile Identifier[] discoveredSoundIds = new Identifier[0];

    private CustomSongs() {}

    // No-op: we no longer create or manage a user pack. Users should add a resource pack
    // that contains assets/phonkedit/sounds.json and assets/phonkedit/sounds/... themselves.
    public static void initializeOnClientStart() { }

    public static void onResourceReload(ResourceManager manager) {
        scanLayeredSounds(manager);
    }

    public static SoundEvent[] getCurrentPhonkSoundEvents(SoundEvent[] builtin) {
        Identifier[] ids = discoveredSoundIds;
        if (ids.length == 0) return builtin;
        List<SoundEvent> events = new ArrayList<>(ids.length);
        for (Identifier id : ids) events.add(SoundEvent.of(id));
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
        // Prefer song# keys if present
        if (!keys.isEmpty()) {
            List<String> songKeys = new ArrayList<>();
            for (String k : keys) {
                if (k.startsWith("song")) {
                    boolean ok = k.length() > 4;
                    for (int i = 4; ok && i < k.length(); i++) {
                        char c = k.charAt(i);
                        if (c < '0' || c > '9') ok = false;
                    }
                    if (ok) songKeys.add(k);
                }
            }
            if (!songKeys.isEmpty()) keys = new HashSet<>(songKeys);
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
}
