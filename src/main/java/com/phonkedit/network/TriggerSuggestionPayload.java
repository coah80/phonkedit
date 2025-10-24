package com.phonkedit.network;

import com.phonkedit.PhonkEditMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TriggerSuggestionPayload(String reason) implements CustomPayload {
    public static final CustomPayload.Id<TriggerSuggestionPayload> ID = new CustomPayload.Id<>(Identifier.of(PhonkEditMod.MOD_ID, "trigger_suggestion"));
    public static final PacketCodec<RegistryByteBuf, TriggerSuggestionPayload> CODEC = PacketCodec.of(TriggerSuggestionPayload::write, TriggerSuggestionPayload::read);

    private static void write(TriggerSuggestionPayload payload, RegistryByteBuf buf) {
        buf.writeString(payload.reason());
    }

    private static TriggerSuggestionPayload read(RegistryByteBuf buf) {
        return new TriggerSuggestionPayload(buf.readString());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
