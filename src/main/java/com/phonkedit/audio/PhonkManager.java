package com.phonkedit.audio;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.ModSounds;
import com.phonkedit.config.ModConfig;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class PhonkManager {
    public static final class TrackSelection {
        public final SoundEvent event;
        public final float pitch;

        private TrackSelection(SoundEvent event, float pitch) {
            this.event = event;
            this.pitch = pitch;
        }
    }

    private static final PhonkManager INSTANCE = new PhonkManager();
    private final Random random = new Random();
    private SoundInstance currentInstance;
    private SoundEvent currentSoundEvent;
    private float currentPitch = 1.0f;
    private boolean isPlaying;

    public static PhonkManager getInstance() {
        return INSTANCE;
    }

    public TrackSelection pickRandomTrackAndPitch() {
        SoundEvent event = chooseRandomEvent();
        double min = ModConfig.INSTANCE.phonkPitchMin;
        double max = ModConfig.INSTANCE.phonkPitchMax;
        if (max < min) {
            double temp = min;
            min = max;
            max = temp;
        }
        double range = Math.max(0.0, max - min);
        float pitch = (float) (range == 0.0 ? min : min + random.nextDouble() * range);
        return new TrackSelection(event, pitch);
    }

    public void playRandomTrack() {
        TrackSelection selection = pickRandomTrackAndPitch();
        playSelection(selection.event, selection.pitch);
    }

    public void playTrackById(Identifier id, float pitch) {
    SoundEvent event = Registries.SOUND_EVENT.getOrEmpty(id).orElse(null);
        if (event == null) {
            event = chooseRandomEvent();
        }
        playSelection(event, pitch);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isCurrentTrackPlaying() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || currentInstance == null) {
            return false;
        }
        return client.getSoundManager().isPlaying(currentInstance);
    }

    public SoundEvent getCurrentSoundEvent() {
        return currentSoundEvent;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    public void stopAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && currentInstance != null) {
            client.getSoundManager().stop(currentInstance);
        }
        currentInstance = null;
        currentSoundEvent = null;
        isPlaying = false;
    }

    private void playSelection(SoundEvent event, float pitch) {
        if (event == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        stopAll();
    PositionedSoundInstance instance = PositionedSoundInstance.master(event, 1.0f, pitch);
        currentInstance = instance;
        currentSoundEvent = event;
        currentPitch = pitch;
        isPlaying = true;
        client.getSoundManager().play(instance);
        PhonkEditMod.LOGGER.info("Playing phonk track {}", Registries.SOUND_EVENT.getId(event));
    }

    private SoundEvent chooseRandomEvent() {
        SoundEvent[] sounds = ModSounds.getAllPhonkSounds();
        if (sounds.length == 0) {
            return null;
        }
        return sounds[random.nextInt(sounds.length)];
    }
}
