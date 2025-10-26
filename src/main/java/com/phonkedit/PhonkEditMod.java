package com.phonkedit;

import com.phonkedit.config.ModConfig;
import com.phonkedit.network.NetworkHandler;
import com.phonkedit.state.PhonkCurseState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhonkEditMod implements ModInitializer {
    public static final String MOD_ID = "phonkedit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Phonk Edit");
        ModConfig.load();
        NetworkHandler.initServer();
        ModBlocks.initialize();
    ModItemGroups.initialize();
        ModAdvancements.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("phonkreset").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
            PhonkCurseState state = PhonkCurseState.get(context.getSource().getServer());
            state.setCurseBroken(false);
            NetworkHandler.broadcastCurseStatus(context.getSource().getServer(), false);
            context.getSource().sendFeedback(() -> Text.literal("Phonk curse reset."), true);
            return 1;
        })));
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }
            String reason = null;
            EntityType<?> type = killed.getType();
            if (type == EntityType.ENDER_DRAGON) {
                reason = "boss:dragon";
            } else if (type == EntityType.WITHER) {
                reason = "boss:wither";
            } else if (type == EntityType.WARDEN) {
                reason = "boss:warden";
            } else if (type == EntityType.ELDER_GUARDIAN) {
                reason = "boss:elder_guardian";
            }
            if (reason != null) {
                NetworkHandler.sendTriggerSuggestion(player, reason);
            }
        });

        if (!FabricLoader.getInstance().isModLoaded("modmenu") && !ModConfig.INSTANCE.modMenuDisclaimerShown) {
            LOGGER.warn("Phonk Edit: Mod Menu not detected. Install Mod Menu for the full configuration UI or continue using the bundled fallback screen.");
        }

        ModSounds.initialize();
    }
}
