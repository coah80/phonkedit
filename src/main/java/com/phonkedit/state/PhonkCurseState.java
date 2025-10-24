package com.phonkedit.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class PhonkCurseState extends PersistentState {
    private static final String STORAGE_ID = "phonkedit_curse";
    private static final Type<PhonkCurseState> TYPE = new Type<>(PhonkCurseState::new, PhonkCurseState::fromNbt, null);
    private boolean curseBroken;

    public static PhonkCurseState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            throw new IllegalStateException("Overworld not available");
        }
        return overworld.getPersistentStateManager().getOrCreate(TYPE, STORAGE_ID);
    }

    private static PhonkCurseState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PhonkCurseState state = new PhonkCurseState();
        state.curseBroken = nbt.getBoolean("curseBroken");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("curseBroken", curseBroken);
        return nbt;
    }

    public boolean isCurseBroken() {
        return curseBroken;
    }

    public void setCurseBroken(boolean value) {
        if (curseBroken != value) {
            curseBroken = value;
            markDirty();
        }
    }
}
