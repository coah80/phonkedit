package com.phonkedit.client;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.audio.PhonkManager;
import com.phonkedit.config.ModConfig;
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
    
    private static long freezeActivationTime = 0;
    private static final long SHAKE_DURATION = 500;
    private static final int SHAKE_INTENSITY = 8;
    private static final int MAX_BLUR_LAYERS = 3;

    @Override
    public void onInitializeClient() {
        PhonkEditMod.LOGGER.info("Phonk Edit client initialized");
        
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
        
        Renderer2d.renderQuad(
            drawContext.getMatrices(),
            new Color(128, 128, 128, 200),
            0, 0,
            screenWidth, screenHeight
        );
        
        long timeSinceActivation = System.currentTimeMillis() - freezeActivationTime;
        int shakeOffsetX = 0;
        int shakeOffsetY = 0;
        double blurStrength = 0.0;
        
        if (timeSinceActivation < SHAKE_DURATION) {
            double shakeProgress = (double) timeSinceActivation / SHAKE_DURATION;
            
            double easeOut = 1.0 - Math.pow(1.0 - shakeProgress, 3);
            double shakeIntensity = (1.0 - easeOut) * SHAKE_INTENSITY * ModConfig.INSTANCE.shakeIntensity;
            
            blurStrength = (1.0 - easeOut);
            
            shakeOffsetX = (int) ((Math.random() * 2 - 1) * shakeIntensity);
            shakeOffsetY = (int) ((Math.random() * 2 - 1) * shakeIntensity);
        }
        
        Identifier skullTexture = SKULL_TEXTURES[currentSkullIndex];
        int skullSize = 256;
        int skullX = (screenWidth - skullSize) / 2 + shakeOffsetX;
        int skullY = (screenHeight - skullSize) / 2 + shakeOffsetY;
        
        RenderSystem.setShaderTexture(0, skullTexture);
        
        if (blurStrength > 0.01) {
            int blurLayers = (int) Math.ceil(blurStrength * MAX_BLUR_LAYERS);
            float blurAlpha = (float) (0.3 * blurStrength / blurLayers);
            
            for (int i = 1; i <= blurLayers; i++) {
                float blurOffset = (float) (i * 2.5 * blurStrength);
                
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, blurAlpha);
                
                Renderer2d.renderTexture(
                    drawContext.getMatrices(),
                    skullX + blurOffset, skullY,
                    skullSize, skullSize,
                    0, 0,
                    skullSize, skullSize,
                    skullSize, skullSize
                );
                Renderer2d.renderTexture(
                    drawContext.getMatrices(),
                    skullX - blurOffset, skullY,
                    skullSize, skullSize,
                    0, 0,
                    skullSize, skullSize,
                    skullSize, skullSize
                );
                Renderer2d.renderTexture(
                    drawContext.getMatrices(),
                    skullX, skullY + blurOffset,
                    skullSize, skullSize,
                    0, 0,
                    skullSize, skullSize,
                    skullSize, skullSize
                );
                Renderer2d.renderTexture(
                    drawContext.getMatrices(),
                    skullX, skullY - blurOffset,
                    skullSize, skullSize,
                    0, 0,
                    skullSize, skullSize,
                    skullSize, skullSize
                );
            }
            
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        
        Renderer2d.renderTexture(
            drawContext.getMatrices(),
            skullX, skullY,
            skullSize, skullSize,
            0, 0,
            skullSize, skullSize,
            skullSize, skullSize
        );
        
        RenderSystem.disableBlend();
    }

    public static void activateFreezeEffect() {
        isFreezeModeActive = true;
        freezeActivationTime = System.currentTimeMillis();
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
