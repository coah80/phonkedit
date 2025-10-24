package com.phonkedit.network;

import com.phonkedit.PhonkEditMod;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EffectSyncPayload(String soundId, float pitch, Optional<byte[]> imagePng) implements CustomPayload {
    public static final CustomPayload.Id<EffectSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(PhonkEditMod.MOD_ID, "effect_sync"));
    public static final PacketCodec<RegistryByteBuf, EffectSyncPayload> CODEC = PacketCodec.of(EffectSyncPayload::write, EffectSyncPayload::read);

    private static void write(EffectSyncPayload payload, RegistryByteBuf buf) {
        buf.writeString(payload.soundId());
        buf.writeFloat(payload.pitch());
        if (payload.imagePng().isPresent()) {
            buf.writeBoolean(true);
            buf.writeByteArray(payload.imagePng().get());
        } else {
            buf.writeBoolean(false);
        }
    }

    private static EffectSyncPayload read(RegistryByteBuf buf) {
        String soundId = buf.readString();
        float pitch = buf.readFloat();
        boolean hasImage = buf.readBoolean();
        Optional<byte[]> image = hasImage ? Optional.of(buf.readByteArray()) : Optional.empty();
        return new EffectSyncPayload(soundId, pitch, image);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
