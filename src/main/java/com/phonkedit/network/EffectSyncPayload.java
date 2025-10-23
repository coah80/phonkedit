package com.phonkedit.network;

import com.phonkedit.PhonkEditMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.Optional;

/**
 * Payload for syncing the freeze effect: sound id, pitch, and optional skull PNG image bytes.
 */
public record EffectSyncPayload(String soundId, float pitch, Optional<byte[]> imagePng) implements CustomPayload {
    public static final Id<EffectSyncPayload> ID = new Id<>(Identifier.of(PhonkEditMod.MOD_ID, "activate_effect"));

    public static final PacketCodec<RegistryByteBuf, EffectSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, EffectSyncPayload::soundId,
            PacketCodecs.FLOAT, EffectSyncPayload::pitch,
            PacketCodecs.optional(PacketCodecs.BYTE_ARRAY), EffectSyncPayload::imagePng,
            EffectSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
