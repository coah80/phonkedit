package com.phonkedit.client;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.audio.PhonkManager;
import com.phonkedit.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;

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
    private static final long EFFECT_DELAY = 300; // 0.3 second delay
    private static long pendingEffectTime = 0;
    
    private static long freezeActivationTime = 0;
    private static final long SHAKE_DURATION = 500;
    private static final int SHAKE_INTENSITY = 8;
    private static final int MAX_BLUR_LAYERS = 3;
    private static boolean wasAttackingEntityLastTick = false;
    private static float lastHealth = -1f;
    private static boolean lastHealthInitialized = false;
    private static boolean wasBelowLowHealth = false;
    private static boolean requestCapture = false;
    private static Identifier freezeTextureId = null;
    private static NativeImageBackedTexture freezeTexture = null;
    private static int freezeTexWidth = 0;
    private static int freezeTexHeight = 0;
    private static Vec3d preservedVelocity = Vec3d.ZERO;
    private static boolean hasPreservedVelocity = false;
    private static boolean preservedSprinting = false;

    @Override
    public void onInitializeClient() {
        PhonkEditMod.LOGGER.info("Phonk Edit client initialized");
        
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (MinecraftClient.getInstance().currentScreen != null) {
                return;
            }
            renderOverlayIfActive(drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check for pending delayed effect
            if (pendingEffectTime > 0 && System.currentTimeMillis() >= pendingEffectTime) {
                pendingEffectTime = 0;
                activateFreezeEffect();
            }
            
            // Auto end effect based on audio playback, fallback to configured duration
            if (isFreezeModeActive) {
                boolean trackKnown = PhonkManager.getInstance().isPlaying();
                boolean stillPlaying = PhonkManager.getInstance().isCurrentTrackPlaying();
                if (trackKnown) {
                    if (!stillPlaying) {
                        endFreezeEffect();
                    }
                } else {
                    if (System.currentTimeMillis() - freezeActivationTime >= ModConfig.INSTANCE.effectDuration) {
                        endFreezeEffect();
                    }
                }

                if (isFreezeModeActive && client.player != null) {
                    client.player.setVelocity(0.0, 0.0, 0.0);
                    if (client.player.input != null) {
                        client.player.input.movementForward = 0.0f;
                        client.player.input.movementSideways = 0.0f;
                        client.player.input.jumping = false;
                        client.player.input.sneaking = false;
                    }
                    client.player.setSprinting(false);
                }

            }

            // Entity attack triggers
            GameOptions options = client.options;
            boolean attackKey = options.attackKey.isPressed();
            boolean targetIsEntity = client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY;

            if (ModConfig.INSTANCE.triggerOnEntityHit) {
                boolean entityAttackStarted = attackKey && targetIsEntity && !wasAttackingEntityLastTick;
                if (entityAttackStarted) {
                    tryTriggerEffect();
                }
                wasAttackingEntityLastTick = attackKey && targetIsEntity;
            }

            // Damage/health based triggers
            if (client.player != null) {
                float currentHealth = client.player.getHealth();
                if (lastHealthInitialized) {
                    if (ModConfig.INSTANCE.triggerOnDamageTaken && currentHealth < lastHealth - 0.001f) {
                        tryTriggerEffect();
                    }
                }
                boolean below = currentHealth <= ModConfig.INSTANCE.lowHealthThreshold;
                if (ModConfig.INSTANCE.triggerOnLowHealth && below && !wasBelowLowHealth) {
                    tryTriggerEffect();
                }
                wasBelowLowHealth = below;
                lastHealth = currentHealth;
                lastHealthInitialized = true;
            } else {
                lastHealthInitialized = false;
                wasBelowLowHealth = false;
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onWorldJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // Force end effect when disconnecting to allow world to save
            if (isFreezeModeActive) {
                endFreezeEffect();
            }
            pendingEffectTime = 0;
        });
    }
    
    public static void renderOverlayIfActive(DrawContext drawContext) {
        if (!isFreezeModeActive) {
            return;
        }
        if (requestCapture) {
            tryCaptureFreezeFrame();
        }
        renderFreezeOverlay(drawContext);
    }

    public static void renderFreezeOverlay(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        if (freezeTextureId != null && freezeTexWidth > 0 && freezeTexHeight > 0) {
            float sx = screenWidth / (float) Math.max(1, freezeTexWidth);
            float sy = screenHeight / (float) Math.max(1, freezeTexHeight);
            var matrices = drawContext.getMatrices();
            matrices.push();
            matrices.scale(sx, sy, 1.0f);
            drawContext.drawTexture(freezeTextureId, 0, 0, 0, 0, freezeTexWidth, freezeTexHeight, freezeTexWidth, freezeTexHeight);
            matrices.pop();
        }

        if (ModConfig.INSTANCE.darkenScreenDuringEffect) {
            drawContext.fill(0, 0, screenWidth, screenHeight, new Color(0, 0, 0, 110).getRGB());
        }

        if (ModConfig.INSTANCE.showCinematicBars) {
            float targetAspect = 9f / 16f;
            float currentAspect = screenWidth / (float) screenHeight;
            if (Math.abs(currentAspect - targetAspect) > 0.001f) {
                if (currentAspect > targetAspect) {
                    float expectedWidth = screenHeight * targetAspect;
                    int barWidth = Math.round((screenWidth - expectedWidth) / 2f);
                    if (barWidth > 0) {
                        drawContext.fill(0, 0, barWidth, screenHeight, 0xFF000000);
                        drawContext.fill(screenWidth - barWidth, 0, screenWidth, screenHeight, 0xFF000000);
                    }
                } else {
                    float expectedHeight = screenWidth / targetAspect;
                    int barHeight = Math.round((screenHeight - expectedHeight) / 2f);
                    if (barHeight > 0) {
                        drawContext.fill(0, 0, screenWidth, barHeight, 0xFF000000);
                        drawContext.fill(0, screenHeight - barHeight, screenWidth, screenHeight, 0xFF000000);
                    }
                }
            }
        }

        if (!ModConfig.INSTANCE.renderSkullOverlay) {
            return;
        }

        long timeSinceActivation = System.currentTimeMillis() - freezeActivationTime;
        int shakeOffsetX = 0;
        int shakeOffsetY = 0;
        double blurStrength = 0.0;
        boolean allowShake = ModConfig.INSTANCE.skullShakeEnabled;
        boolean allowBlur = ModConfig.INSTANCE.skullBlurEnabled;

        if ((allowShake || allowBlur) && timeSinceActivation < SHAKE_DURATION) {
            double shakeProgress = (double) timeSinceActivation / SHAKE_DURATION;
            double easeOut = 1.0 - Math.pow(1.0 - shakeProgress, 3);
            double effectFalloff = (1.0 - easeOut);

            if (allowShake) {
                double shakeIntensity = effectFalloff * SHAKE_INTENSITY * ModConfig.INSTANCE.shakeIntensity;
                shakeOffsetX = (int) ((Math.random() * 2 - 1) * shakeIntensity);
                shakeOffsetY = (int) ((Math.random() * 2 - 1) * shakeIntensity);
            }

            if (allowBlur) {
                blurStrength = effectFalloff;
            }
        }

        int skullSize = 64;
        int hotbarHeight = 22;
        int padding = 40;
        int y = screenHeight - hotbarHeight - skullSize - padding;
        int xCenter = screenWidth / 2 - skullSize / 2;
        Identifier skullCenter = SKULL_TEXTURES[currentSkullIndex];
        int shakeXCenter = shakeOffsetX;
        int shakeYCenter = shakeOffsetY;

        if (allowBlur && blurStrength > 0.01) {
            int blurLayers = Math.max(1, (int) Math.ceil(blurStrength * (MAX_BLUR_LAYERS - 1)));
            float blurAlpha = (float) (0.25 * blurStrength / blurLayers);

            RenderSystem.enableBlend();
            for (int i = 1; i <= blurLayers; i++) {
                float blurOffset = (float) (i * 1.8 * blurStrength);

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, blurAlpha);
                drawContext.drawTexture(skullCenter, (int) (xCenter + shakeXCenter + blurOffset), y + shakeYCenter, 0, 0, skullSize, skullSize, skullSize, skullSize);
                drawContext.drawTexture(skullCenter, (int) (xCenter + shakeXCenter - blurOffset), y + shakeYCenter, 0, 0, skullSize, skullSize, skullSize, skullSize);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }

        drawContext.drawTexture(skullCenter, xCenter + shakeXCenter, y + shakeYCenter, 0, 0, skullSize, skullSize, skullSize, skullSize);
    }

    public static void activateFreezeEffect() {
        isFreezeModeActive = true;
        freezeActivationTime = System.currentTimeMillis();
        currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.length);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            preservedVelocity = client.player.getVelocity();
            preservedSprinting = client.player.isSprinting();
            hasPreservedVelocity = true;
        } else {
            preservedVelocity = Vec3d.ZERO;
            preservedSprinting = false;
            hasPreservedVelocity = false;
        }

        PhonkManager.getInstance().playRandomTrack();
        requestCapture = true;
        PhonkEditMod.LOGGER.info("Activated freeze effect");
    }

    public static void endFreezeEffect() {
        isFreezeModeActive = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (hasPreservedVelocity && client != null && client.player != null) {
            client.player.setVelocity(preservedVelocity);
            client.player.velocityModified = true;
            client.player.setSprinting(preservedSprinting);
        }
        hasPreservedVelocity = false;
        preservedVelocity = Vec3d.ZERO;
        preservedSprinting = false;

    PhonkManager.getInstance().stopAll();
    clearFreezeFrame();
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
    pendingEffectTime = 0;
    hasPreservedVelocity = false;
    preservedVelocity = Vec3d.ZERO;
    preservedSprinting = false;
    }

    public static boolean canTrigger() {
        return System.currentTimeMillis() - worldJoinTime >= WORLD_JOIN_COOLDOWN;
    }

    public static void onBlockBroken() {
        if (!ModConfig.INSTANCE.triggerOnBlockBreak) {
            return;
        }
        scheduleEffect();
    }

    private static void scheduleEffect() {
        if (!ModConfig.INSTANCE.enablePhonkEffect) return;
        if (!canTrigger()) return;
        if (isFreezeModeActive) return;
        if (pendingEffectTime > 0) return; // Already scheduled
        if (Math.random() <= ModConfig.INSTANCE.triggerChance) {
            pendingEffectTime = System.currentTimeMillis() + EFFECT_DELAY;
        }
    }

    private static void tryTriggerEffect() {
        scheduleEffect();
    }

    private static void tryCaptureFreezeFrame() {
        requestCapture = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        try {
            Framebuffer fb = client.getFramebuffer();
            int w = client.getWindow().getFramebufferWidth();
            int h = client.getWindow().getFramebufferHeight();

            // Attempt to call a NativeImage-returning takeScreenshot via reflection (if present in mappings)
            NativeImage image = null;
            try {
                Class<?> sr = Class.forName("net.minecraft.client.util.ScreenshotRecorder");
                java.lang.reflect.Method method = null;
                for (java.lang.reflect.Method m : sr.getDeclaredMethods()) {
                    if (m.getName().equals("takeScreenshot") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(fb.getClass())) {
                        if (NativeImage.class.isAssignableFrom(m.getReturnType())) {
                            method = m; break;
                        }
                    }
                }
                if (method != null) {
                    method.setAccessible(true);
                    image = (NativeImage) method.invoke(null, fb);
                }
            } catch (Throwable ignored) { }

            if (image == null) {
                // Fallback: allocate and read pixels via framebuffer (if supported)
                image = new NativeImage(w, h, false);
                fb.beginRead();
                image.loadFromTextureImage(0, false);
                fb.endRead();
            }

            if (ModConfig.INSTANCE.grayscaleFreezeFrame) {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int argb = image.getColor(x, y);
                        int a = (argb >> 24) & 0xFF;
                        int r = (argb) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = (argb >> 16) & 0xFF;
                        int gray = (int) Math.min(255, Math.round(0.2126 * r + 0.7152 * g + 0.0722 * b));
                        int newAbgr = (a << 24) | (gray << 16) | (gray << 8) | gray;
                        image.setColor(x, y, newAbgr);
                    }
                }
            }

            for (int yTop = 0, yBot = h - 1; yTop < yBot; yTop++, yBot--) {
                for (int x = 0; x < w; x++) {
                    int top = image.getColor(x, yTop);
                    int bot = image.getColor(x, yBot);
                    image.setColor(x, yTop, bot);
                    image.setColor(x, yBot, top);
                }
            }

            // Upload to a dynamic texture and keep id
            clearFreezeFrame();
            freezeTexture = new NativeImageBackedTexture(image);
            freezeTextureId = Identifier.of("phonkedit", "freeze_frame");
            TextureManager tm = client.getTextureManager();
            tm.registerTexture(freezeTextureId, freezeTexture);
            freezeTexWidth = w; freezeTexHeight = h;
        } catch (Throwable t) {
            PhonkEditMod.LOGGER.warn("Failed to capture freeze frame: {}", t.toString());
        }
    }

    private static void clearFreezeFrame() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && freezeTextureId != null) {
            try {
                client.getTextureManager().destroyTexture(freezeTextureId);
            } catch (Throwable ignored) { }
        }
        if (freezeTexture != null) {
            try { freezeTexture.close(); } catch (Throwable ignored) { }
        }
        freezeTexture = null;
        freezeTextureId = null;
        freezeTexWidth = 0; freezeTexHeight = 0;
    }

    
}
