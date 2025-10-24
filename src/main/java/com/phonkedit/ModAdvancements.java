package com.phonkedit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ModAdvancements {
    private static final Identifier ROOT_ADVANCEMENT = Identifier.of(PhonkEditMod.MOD_ID, "root");
    private static final Identifier IT_STARTS_ADVANCEMENT = Identifier.of(PhonkEditMod.MOD_ID, "tasks/it_starts");
    private static final Identifier SKULL_ME_ONCE_ADVANCEMENT = Identifier.of(PhonkEditMod.MOD_ID, "tasks/skull_me_once");

    private static final Set<UUID> pendingPlayers = new HashSet<>();

    private ModAdvancements() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            AdvancementEntry root = server.getAdvancementLoader().get(ROOT_ADVANCEMENT);
            AdvancementEntry toast = server.getAdvancementLoader().get(IT_STARTS_ADVANCEMENT);
            boolean pendingRoot = root != null && !player.getAdvancementTracker().getProgress(root).isDone();
            boolean pendingToast = toast != null && !player.getAdvancementTracker().getProgress(toast).isDone();
            if (pendingRoot || pendingToast) {
                pendingPlayers.add(player.getUuid());
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(ModAdvancements::handlePendingPlayers);
    }

    private static void handlePendingPlayers(MinecraftServer server) {
        if (pendingPlayers.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = pendingPlayers.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null || player.isDisconnected()) {
                iterator.remove();
                continue;
            }
            AdvancementEntry root = server.getAdvancementLoader().get(ROOT_ADVANCEMENT);
            if (root != null && !player.getAdvancementTracker().getProgress(root).isDone()) {
                player.getAdvancementTracker().grantCriterion(root, "first_tick");
            }
            AdvancementEntry toast = server.getAdvancementLoader().get(IT_STARTS_ADVANCEMENT);
            if (toast != null && !player.getAdvancementTracker().getProgress(toast).isDone()) {
                player.getAdvancementTracker().grantCriterion(toast, "recipe_unlocked");
            }
            iterator.remove();
        }
    }

    public static void grantSkullMeOnce(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AdvancementEntry advancement = server.getAdvancementLoader().get(SKULL_ME_ONCE_ADVANCEMENT);
        if (advancement == null) {
            return;
        }
        if (player.getAdvancementTracker().getProgress(advancement).isDone()) {
            return;
        }
        player.getAdvancementTracker().grantCriterion(advancement, "ritual_complete");
    }
}
