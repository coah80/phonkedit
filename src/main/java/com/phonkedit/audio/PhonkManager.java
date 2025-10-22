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

    public static PhonkManager getInstance() {
        return INSTANCE;
    }

    public SoundInstance playRandomTrack() {
        MinecraftClient client = MinecraftClient.getInstance();
        
        SoundEvent[] sounds = ModSounds.getAllPhonkSounds();
        SoundEvent selectedSound = sounds[random.nextInt(sounds.length)];

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
    PhonkEditMod.LOGGER.info("Playing phonk track (pitch {})", String.format("%.2f", pitch));
        return instance;
    }

    public void stopAll() {
        isPlaying = false;
        if (currentInstance != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.getSoundManager().stop(currentInstance);
            currentInstance = null;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isCurrentTrackPlaying() {
        if (currentInstance == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSoundManager().isPlaying(currentInstance);
    }
}
