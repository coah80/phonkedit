package com.phonkedit.audio;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.ModSounds;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;

import java.util.Random;

public class PhonkManager {
    private static final PhonkManager INSTANCE = new PhonkManager();
    private final Random random = new Random();
    private boolean isPlaying = false;
    private SoundInstance currentInstance = null;
    private SoundEvent currentSoundEvent = null;
    private boolean currentIsCustom = false;
    private long customCutoffDeadlineMs = 0L;

    public static final class TrackSelection {
        public final SoundEvent event;
        public final float pitch;

        public TrackSelection(SoundEvent event, float pitch) {
            this.event = event;
            this.pitch = pitch;
        }
    }

    public static PhonkManager getInstance() {
        return INSTANCE;
    }

    public TrackSelection pickRandomTrackAndPitch() {
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

        return new TrackSelection(selectedSound, pitch);
    }

    public SoundInstance playRandomTrack() {
        TrackSelection sel = pickRandomTrackAndPitch();
        return playTrack(sel.event, sel.pitch);
    }

    public SoundInstance playTrack(SoundEvent event, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        currentSoundEvent = event;
        currentIsCustom = CustomSongs.isCustom(event);
        PositionedSoundInstance instance = PositionedSoundInstance.master(event, 1.0f, pitch);
        client.getSoundManager().play(instance);
        currentInstance = instance;
        isPlaying = true;
        if (currentIsCustom) {
            customCutoffDeadlineMs = System.currentTimeMillis() + 5000L;
        } else {
            customCutoffDeadlineMs = 0L;
        }
        PhonkEditMod.LOGGER.info("Playing phonk track (pitch {}, custom: {})", String.format("%.2f", pitch), currentIsCustom);
        return instance;
    }

    public SoundInstance playTrackById(Identifier id, float pitch) {
        for (SoundEvent e : ModSounds.getAllPhonkSounds()) {
            Identifier eid = Registries.SOUND_EVENT.getId(e);
            if (eid != null && eid.equals(id)) {
                return playTrack(e, pitch);
            }
        }
        PhonkEditMod.LOGGER.warn("Unknown sound id '{}', falling back to random", id);
        return playRandomTrack();
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
