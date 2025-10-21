package com.phonkedit;

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

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of("phonkedit", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        PhonkEditMod.LOGGER.info("Registered phonk sounds");
    }

    public static SoundEvent[] getAllPhonkSounds() {
        return new SoundEvent[]{PHONK1, PHONK2, PHONK3, PHONK4, PHONK5};
    }
}
