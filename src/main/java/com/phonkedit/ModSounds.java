package com.phonkedit;

import com.phonkedit.audio.CustomSongs;
import com.phonkedit.config.ModConfig;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent PHONK1 = registerSound("phonk1");
    public static final SoundEvent PHONK2 = registerSound("phonk2");
    public static final SoundEvent PHONK3 = registerSound("phonk3");
    public static final SoundEvent PHONK4 = registerSound("phonk4");
    public static final SoundEvent PHONK5 = registerSound("phonk5");
    public static final SoundEvent PHONK6 = registerSound("phonk6");
    public static final SoundEvent PHONK7 = registerSound("phonk7");
    public static final SoundEvent PHONK8 = registerSound("phonk8");
    public static final SoundEvent PHONK9 = registerSound("phonk9");
    public static final SoundEvent PHONK10 = registerSound("phonk10");
    public static final SoundEvent PHONK11 = registerSound("phonk11");
    public static final SoundEvent PHONK12 = registerSound("phonk12");

    private static final SoundEvent[] BUILTIN_SOUNDS = new SoundEvent[] {
        PHONK1, PHONK2, PHONK3, PHONK4, PHONK5,
        PHONK6, PHONK7, PHONK8, PHONK9, PHONK10,
        PHONK11, PHONK12
    };

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of("phonkedit", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        PhonkEditMod.LOGGER.info("Registered {} built-in phonk sounds", BUILTIN_SOUNDS.length);
    }

    public static SoundEvent[] getAllPhonkSounds() {
        var custom = CustomSongs.getCustomSongEvents();
        if (!custom.isEmpty()) {
            if (ModConfig.INSTANCE.mixBuiltinWithCustomSongs) {
                SoundEvent[] combined = new SoundEvent[BUILTIN_SOUNDS.length + custom.size()];
                System.arraycopy(BUILTIN_SOUNDS, 0, combined, 0, BUILTIN_SOUNDS.length);
                for (int i = 0; i < custom.size(); i++) {
                    combined[BUILTIN_SOUNDS.length + i] = custom.get(i);
                }
                return combined;
            } else {
                return custom.toArray(new SoundEvent[0]);
            }
        }
        return BUILTIN_SOUNDS.clone();
    }
}
