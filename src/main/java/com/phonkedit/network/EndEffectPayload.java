package com.phonkedit.network;

import com.phonkedit.PhonkEditMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EndEffectPayload() implements CustomPayload {
    public static final CustomPayload.Id<EndEffectPayload> ID = new CustomPayload.Id<>(Identifier.of(PhonkEditMod.MOD_ID, "end_effect"));
    public static final PacketCodec<RegistryByteBuf, EndEffectPayload> CODEC = PacketCodec.of((p, buf) -> {}, buf -> new EndEffectPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
