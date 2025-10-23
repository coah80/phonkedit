package com.phonkedit.audio;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.ModSounds;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class PhonkManager {
    private static final PhonkManager INSTANCE = new PhonkManager();
    private final Random random = new Random();
    private boolean isPlaying = false;
    private SoundInstance currentInstance = null;
    private SoundEvent currentSoundEvent = null;
    private boolean currentIsCustom = false;
    private long customCutoffDeadlineMs = 0L;

    public static PhonkManager getInstance() {
        return INSTANCE;
    }

    public SoundInstance playRandomTrack() {
        MinecraftClient client = MinecraftClient.getInstance();
        
        SoundEvent[] sounds = ModSounds.getAllPhonkSounds();
        SoundEvent selectedSound = sounds[random.nextInt(sounds.length)];
        currentSoundEvent = selectedSound;
        currentIsCustom = CustomSongs.isCustom(selectedSound);

        double minPitch = ModConfig.INSTANCE.phonkPitchMin;
        double maxPitch = ModConfig.INSTANCE.phonkPitchMax;
        if (maxPitch < minPitch) {
            double swap = minPitch;
            minPitch = maxPitch;
            maxPitch = swap;
        }
        minPitch = MathHelper.clamp(minPitch, 0.5, 2.0);
        maxPitch = MathHelper.clamp(maxPitch, 0.5, 2.0);
        double range = Math.max(0.0, maxPitch - minPitch);
        float pitch = (float) (range <= 0.0001 ? minPitch : (minPitch + random.nextDouble() * range));
        pitch = MathHelper.clamp(pitch, 0.5f, 2.0f);

        PositionedSoundInstance instance = PositionedSoundInstance.master(selectedSound, 1.0f, pitch);
        client.getSoundManager().play(instance);

        currentInstance = instance;
        isPlaying = true;
        if (currentIsCustom) {
            customCutoffDeadlineMs = System.currentTimeMillis() + 5000L; // 5 seconds hard cap for custom songs
        } else {
            customCutoffDeadlineMs = 0L;
        }
        PhonkEditMod.LOGGER.info("Playing phonk track (pitch {}, custom: {})", String.format("%.2f", pitch), currentIsCustom);
        return instance;
    }

    public void stopAll() {
        isPlaying = false;
        if (currentInstance != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.getSoundManager().stop(currentInstance);
            currentInstance = null;
        }
        currentSoundEvent = null;
        currentIsCustom = false;
        customCutoffDeadlineMs = 0L;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isCurrentTrackPlaying() {
        if (currentInstance == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        // Enforce 5s cutoff for custom songs: stop the sound but keep isPlaying flag
        if (currentIsCustom && customCutoffDeadlineMs > 0L && System.currentTimeMillis() >= customCutoffDeadlineMs) {
            client.getSoundManager().stop(currentInstance);
            return false;
        }
        return client.getSoundManager().isPlaying(currentInstance);
    }

    public SoundEvent getCurrentSoundEvent() {
        return currentSoundEvent;
    }
}
