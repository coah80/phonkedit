package com.phonkedit.network;

import com.phonkedit.PhonkEditMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CurseStatusPayload(boolean curseBroken) implements CustomPayload {
    public static final CustomPayload.Id<CurseStatusPayload> ID = new CustomPayload.Id<>(Identifier.of(PhonkEditMod.MOD_ID, "curse_status"));
    public static final PacketCodec<RegistryByteBuf, CurseStatusPayload> CODEC = PacketCodec.of(CurseStatusPayload::write, CurseStatusPayload::read);

    private static void write(CurseStatusPayload payload, RegistryByteBuf buf) {
        buf.writeBoolean(payload.curseBroken());
    }

    private static CurseStatusPayload read(RegistryByteBuf buf) {
        return new CurseStatusPayload(buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
