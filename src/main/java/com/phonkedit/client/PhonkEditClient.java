package com.phonkedit.client;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.audio.PhonkManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.x150.renderer.event.RenderEvents;
import me.x150.renderer.render.Renderer2d;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.awt.Color;

@Environment(EnvType.CLIENT)
public class PhonkEditClient implements ClientModInitializer {
    
    private static final Identifier[] SKULL_TEXTURES = {
        Identifier.of("phonkedit", "textures/gui/skull1.png"),
        Identifier.of("phonkedit", "textures/gui/skull2.png"),
        Identifier.of("phonkedit", "textures/gui/skull3.png"),
        Identifier.of("phonkedit", "textures/gui/skull4.png"),
        Identifier.of("phonkedit", "textures/gui/skull5.png"),
        Identifier.of("phonkedit", "textures/gui/skull6.png"),
        Identifier.of("phonkedit", "textures/gui/skull7.png"),
        Identifier.of("phonkedit", "textures/gui/skull8.png"),
    };

    private static boolean isFreezeModeActive = false;
    private static int currentSkullIndex = 0;
    private static long worldJoinTime = 0;
    private static final long WORLD_JOIN_COOLDOWN = 3000;

    @Override
    public void onInitializeClient() {
        PhonkEditMod.LOGGER.info("Phonk Edit client initialized");
        
        // Register HUD rendering using Renderer library
        RenderEvents.HUD.register(drawContext -> {
            if (isFreezeModeActive) {
                renderFreezeOverlay(drawContext);
            }
        });
    }
    
    private static void renderFreezeOverlay(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Render greyscale fullscreen overlay using Renderer2d
        Renderer2d.renderQuad(
            drawContext.getMatrices(),
            new Color(128, 128, 128, 200), // Grey with alpha
            0, 0,
            screenWidth, screenHeight
        );
        
        // Render skull texture in center
        Identifier skullTexture = SKULL_TEXTURES[currentSkullIndex];
        int skullSize = 256;
        int skullX = (screenWidth - skullSize) / 2;
        int skullY = (screenHeight - skullSize) / 2;
        
        RenderSystem.setShaderTexture(0, skullTexture);
        Renderer2d.renderTexture(
            drawContext.getMatrices(),
            skullX, skullY,
            skullSize, skullSize,
            0, 0,
            skullSize, skullSize,
            skullSize, skullSize
        );
    }

    public static void activateFreezeEffect() {
        isFreezeModeActive = true;
        currentSkullIndex = (int) (Math.random() * 8);
        PhonkManager.getInstance().playRandomTrack();
        PhonkEditMod.LOGGER.info("Activated freeze effect");
    }

    public static void endFreezeEffect() {
        isFreezeModeActive = false;
        PhonkManager.getInstance().stopAll();
        PhonkEditMod.LOGGER.info("Ended freeze effect");
    }

    public static boolean isFreezeModeActive() {
        return isFreezeModeActive;
    }

    public static Identifier getCurrentSkullTexture() {
        return SKULL_TEXTURES[currentSkullIndex];
    }

    public static void onWorldJoin() {
        worldJoinTime = System.currentTimeMillis();
    }

    public static boolean canTrigger() {
        return System.currentTimeMillis() - worldJoinTime >= WORLD_JOIN_COOLDOWN;
    }
}
