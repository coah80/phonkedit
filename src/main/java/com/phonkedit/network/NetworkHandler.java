package com.phonkedit.network;

import com.phonkedit.config.ModConfig;
import com.phonkedit.state.PhonkCurseState;
import com.phonkedit.state.ServerFreezeState;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkHandler {
    private static boolean registered;

    private NetworkHandler() {
    }

    private static synchronized void ensureRegistered() {
        if (registered) {
            return;
        }
        PayloadTypeRegistry.playC2S().register(EffectSyncPayload.ID, EffectSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EndEffectPayload.ID, EndEffectPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(EffectSyncPayload.ID, EffectSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CurseStatusPayload.ID, CurseStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TriggerSuggestionPayload.ID, TriggerSuggestionPayload.CODEC);
        registered = true;
    }

    public static void initServer() {
        ensureRegistered();
        ServerPlayNetworking.registerGlobalReceiver(EffectSyncPayload.ID, (payload, context) -> context.server().execute(() -> {
            MinecraftServer server = context.server();
            if (PhonkCurseState.get(server).isCurseBroken()) {
                return;
            }
            ServerFreezeState.protectDamageForMillis(Long.MAX_VALUE);
            if (!server.isDedicated() && ModConfig.INSTANCE.pauseServerDuringEffect) {
                ServerFreezeState.freezeWorldForMillis(Long.MAX_VALUE);
            }
            broadcast(server, payload.soundId(), payload.pitch(), payload.imagePng().orElse(null));
        }));
        ServerPlayNetworking.registerGlobalReceiver(EndEffectPayload.ID, (payload, context) -> context.server().execute(() -> {
            ServerFreezeState.endProtectionNow();
        }));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendCurseStatus(handler.player, PhonkCurseState.get(server).isCurseBroken()));
    }

    public static void initClient(Consumer<EffectSyncPayload> effectHandler, Consumer<CurseStatusPayload> statusHandler, Consumer<TriggerSuggestionPayload> triggerHandler) {
        ensureRegistered();
        ClientPlayNetworking.registerGlobalReceiver(EffectSyncPayload.ID, (payload, context) -> context.client().execute(() -> effectHandler.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(CurseStatusPayload.ID, (payload, context) -> context.client().execute(() -> statusHandler.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(TriggerSuggestionPayload.ID, (payload, context) -> context.client().execute(() -> triggerHandler.accept(payload)));
    }

    public static void sendActivateRequestToServer(String soundId, float pitch, byte[] skullPng) {
        ClientPlayNetworking.send(new EffectSyncPayload(soundId, pitch, Optional.ofNullable(skullPng)));
    }

    public static void sendEndEffectToServer() {
        ClientPlayNetworking.send(new EndEffectPayload());
    }

    public static void sendEffectToAll(MinecraftServer server, String soundId, float pitch, byte[] imagePng) {
        ensureRegistered();
        broadcast(server, soundId, pitch, imagePng);
    }

    public static void sendEffectToAll(MinecraftServer server, String soundId, float pitch) {
        sendEffectToAll(server, soundId, pitch, null);
    }

    public static void sendCurseStatus(ServerPlayerEntity player, boolean broken) {
        ensureRegistered();
        ServerPlayNetworking.send(player, new CurseStatusPayload(broken));
    }

    public static void broadcastCurseStatus(MinecraftServer server, boolean broken) {
        ensureRegistered();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new CurseStatusPayload(broken));
        }
    }

    public static void sendTriggerSuggestion(ServerPlayerEntity player, String reason) {
        ensureRegistered();
        ServerPlayNetworking.send(player, new TriggerSuggestionPayload(reason));
    }

    private static void broadcast(MinecraftServer server, String soundId, float pitch, byte[] imagePng) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, new EffectSyncPayload(soundId, pitch, Optional.ofNullable(imagePng)));
        }
    }
}
