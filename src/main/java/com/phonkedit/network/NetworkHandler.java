package com.phonkedit.network;

import com.phonkedit.client.PhonkEditClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

/**
 * Simple networking for syncing the freeze effect across players.
 *
 * Flow:
 * - Client selects a track/pitch and optional skull image PNG (256x256) and sends C2S_REQUEST
 * - Server rebroadcasts to all players in the same server with S2C_ACTIVATE
 * - Clients receiving S2C_ACTIVATE perform the effect locally and play the given track/pitch
 */
public final class NetworkHandler {
    private static boolean TYPES_REGISTERED = false;

    private static synchronized void ensureTypesRegistered() {
        if (TYPES_REGISTERED) return;
        PayloadTypeRegistry.playC2S().register(EffectSyncPayload.ID, EffectSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(EffectSyncPayload.ID, EffectSyncPayload.CODEC);
        TYPES_REGISTERED = true;
    }

    private NetworkHandler() {}

    public static void initServer() {
        ensureTypesRegistered();
        // Listen for client requests and broadcast to everyone
        ServerPlayNetworking.registerGlobalReceiver(EffectSyncPayload.ID, (payload, context) -> {
            // Broadcast to all players on the server thread
            context.server().execute(() -> broadcastActivate(context.server(), payload.soundId(), payload.pitch(), payload.imagePng().orElse(null)));
        });
    }

    private static void broadcastActivate(MinecraftServer server, String soundId, float pitch, byte[] imagePng) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity p : players) {
            ServerPlayNetworking.send(p, new EffectSyncPayload(soundId, pitch, java.util.Optional.ofNullable(imagePng)));
        }
    }

    public static void initClient() {
        ensureTypesRegistered();
        // Register S2C handler to activate effect
        ClientPlayNetworking.registerGlobalReceiver(EffectSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> PhonkEditClient.onActivateFromNetwork(payload.soundId(), payload.pitch(), payload.imagePng().orElse(null)));
        });
    }

    public static void sendActivateRequestToServer(String soundId, float pitch, byte[] skullPng) {
        ClientPlayNetworking.send(new EffectSyncPayload(soundId, pitch, java.util.Optional.ofNullable(skullPng)));
    }
}
