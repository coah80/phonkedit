package com.phonkedit.audio;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

import java.util.Random;

public class PhonkManager {
    private static final PhonkManager INSTANCE = new PhonkManager();
    private final Random random = new Random();
    private boolean isPlaying = false;

    public static PhonkManager getInstance() {
        return INSTANCE;
    }

    public void playRandomTrack() {
        MinecraftClient client = MinecraftClient.getInstance();
        
        SoundEvent[] sounds = ModSounds.getAllPhonkSounds();
        SoundEvent selectedSound = sounds[random.nextInt(sounds.length)];

        // Use PositionedSoundInstance for direct sound playback
        client.getSoundManager().play(PositionedSoundInstance.master(selectedSound, 1.0f));

        isPlaying = true;
        PhonkEditMod.LOGGER.info("Playing phonk track");
    }

    public void stopAll() {
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
