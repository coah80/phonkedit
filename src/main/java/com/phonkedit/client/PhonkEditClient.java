package com.phonkedit.client;

import com.phonkedit.ModSounds;
import com.phonkedit.PhonkEditMod;
import com.phonkedit.audio.PhonkManager;
import com.phonkedit.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class PhonkEditClient implements ClientModInitializer {
    
    private static final Identifier[] BUILTIN_SKULL_TEXTURES = {
        Identifier.of("phonkedit", "textures/gui/skull1.png"),
        Identifier.of("phonkedit", "textures/gui/skull2.png"),
        Identifier.of("phonkedit", "textures/gui/skull3.png"),
        Identifier.of("phonkedit", "textures/gui/skull4.png"),
        Identifier.of("phonkedit", "textures/gui/skull5.png"),
        Identifier.of("phonkedit", "textures/gui/skull6.png"),
        Identifier.of("phonkedit", "textures/gui/skull7.png"),
        Identifier.of("phonkedit", "textures/gui/skull8.png"),
        Identifier.of("phonkedit", "textures/gui/skull9.png"),
        Identifier.of("phonkedit", "textures/gui/skull10.png"),
        Identifier.of("phonkedit", "textures/gui/skull11.png"),
        Identifier.of("phonkedit", "textures/gui/skull12.png"),
        Identifier.of("phonkedit", "textures/gui/skull13.png"),
        Identifier.of("phonkedit", "textures/gui/skull14.png"),
        Identifier.of("phonkedit", "textures/gui/skull15.png"),
        Identifier.of("phonkedit", "textures/gui/skull16.png"),
        Identifier.of("phonkedit", "textures/gui/skull17.png"),
        Identifier.of("phonkedit", "textures/gui/skull18.png"),
        Identifier.of("phonkedit", "textures/gui/skull19.png"),
    };
    private static final Identifier SPECIAL_SKULL_PHONK6 = Identifier.of("phonkedit", "textures/gui/skull20.png");
    private static final List<Identifier> SKULL_TEXTURES = new ArrayList<>();
    private static final List<Identifier> USER_SKULL_TEXTURE_IDS = new ArrayList<>();
    private static final Path USER_IMAGES_DIR = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("phonkedit")
        .resolve("images");
    private static final int SKULL_TEXTURE_SIZE = 256;
    private static final int SKULL_RENDER_SIZE = 256;
    private static final float SKULL_RENDER_SCALE = 0.4f;
    private static int userSkullIdCounter = 0;
    private static int specialSkullIdCounter = 0;
    private static Identifier customSpecialSkullId = null;
    private static Identifier currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
    private static Identifier overrideSkullTexture = null;

    private static void reloadSkullTextureList() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        TextureManager textureManager = client.getTextureManager();
        ResourceManager resourceManager = client.getResourceManager();
        if (resourceManager == null) {
            PhonkEditMod.LOGGER.debug("Skipping skull reload; resource manager not ready yet");
            return;
        }
        clearUserSkullTextures(textureManager);

        SKULL_TEXTURES.clear();
        Collections.addAll(SKULL_TEXTURES, BUILTIN_SKULL_TEXTURES);
        currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
        overrideSkullTexture = null;

        try {
            Files.createDirectories(USER_IMAGES_DIR);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Unable to prepare skull image directory {}", USER_IMAGES_DIR, e);
            currentSkullIndex = Math.min(currentSkullIndex, Math.max(0, SKULL_TEXTURES.size() - 1));
            return;
        }

        exportBuiltinSkulls(resourceManager);
        exportSpecialSkull(resourceManager);
        loadCustomSkulls(textureManager);

        if (SKULL_TEXTURES.isEmpty()) {
            Collections.addAll(SKULL_TEXTURES, BUILTIN_SKULL_TEXTURES);
        }

        currentSkullIndex = Math.min(currentSkullIndex, Math.max(0, SKULL_TEXTURES.size() - 1));

        int customCount = Math.max(0, SKULL_TEXTURES.size() - BUILTIN_SKULL_TEXTURES.length);
        PhonkEditMod.LOGGER.info("Loaded {} skull textures ({} custom)", SKULL_TEXTURES.size(), customCount);
        if (customCount > 0) {
            PhonkEditMod.LOGGER.info("Custom skull directory: {}", USER_IMAGES_DIR.toAbsolutePath());
        }
    }

    private static void clearUserSkullTextures(TextureManager textureManager) {
        if (!USER_SKULL_TEXTURE_IDS.isEmpty()) {
            for (Identifier id : USER_SKULL_TEXTURE_IDS) {
                textureManager.destroyTexture(id);
            }
            USER_SKULL_TEXTURE_IDS.clear();
        }
        if (customSpecialSkullId != null) {
            textureManager.destroyTexture(customSpecialSkullId);
            customSpecialSkullId = null;
        }
        currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
        userSkullIdCounter = 0;
        specialSkullIdCounter = 0;
        overrideSkullTexture = null;
    }

    private static void exportBuiltinSkulls(ResourceManager resourceManager) {
        for (Identifier id : BUILTIN_SKULL_TEXTURES) {
            String fileName = getFileName(id);
            Path target = USER_IMAGES_DIR.resolve(fileName);
            if (Files.exists(target)) {
                continue;
            }
            resourceManager.getResource(id).ifPresent(res -> {
                try (InputStream inputStream = res.getInputStream()) {
                    Files.copy(inputStream, target);
                } catch (IOException ex) {
                    PhonkEditMod.LOGGER.warn("Failed to export default skull {}", fileName, ex);
                }
            });
        }
    }

    private static void exportSpecialSkull(ResourceManager resourceManager) {
        String fileName = getFileName(SPECIAL_SKULL_PHONK6);
        Path target = USER_IMAGES_DIR.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        resourceManager.getResource(SPECIAL_SKULL_PHONK6).ifPresent(res -> {
            try (InputStream inputStream = res.getInputStream()) {
                Files.copy(inputStream, target);
            } catch (IOException ex) {
                PhonkEditMod.LOGGER.warn("Failed to export special skull {}", fileName, ex);
            }
        });
    }

    private static void loadCustomSkulls(TextureManager textureManager) {
        try (Stream<Path> paths = Files.list(USER_IMAGES_DIR)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return lower.endsWith(".png");
                    })
                    .sorted()
                    .forEach(path -> {
                        String baseName = stripExtension(path.getFileName().toString()).toLowerCase(Locale.ROOT);
                        boolean special = baseName.equals("skull20");
                        if (special && customSpecialSkullId != null) {
                            return;
                        }
                        registerCustomSkull(path, textureManager, special);
                    });
        } catch (IOException e) {
            PhonkEditMod.LOGGER.warn("Failed to scan custom skulls in {}", USER_IMAGES_DIR, e);
        }
    }

    private static void registerCustomSkull(Path imagePath, TextureManager textureManager, boolean special) {
        try (NativeImage source = NativeImage.read(Files.newInputStream(imagePath))) {
            NativeImage scaled = scaleToSquare(source, SKULL_TEXTURE_SIZE);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(scaled);
            String baseName = stripExtension(imagePath.getFileName().toString());
            String sanitized = sanitizeResourceName(baseName);
            Identifier id;
            if (special) {
                String texturePath = "user/skull_special/" + sanitized + "_" + (specialSkullIdCounter++);
                id = Identifier.of(PhonkEditMod.MOD_ID, texturePath);
            } else {
                String texturePath = "user/skull/" + sanitized + "_" + (userSkullIdCounter++);
                id = Identifier.of(PhonkEditMod.MOD_ID, texturePath);
            }
            textureManager.registerTexture(id, texture);
            if (special) {
                customSpecialSkullId = id;
                currentSpecialSkullTexture = id;
                PhonkEditMod.LOGGER.info("Registered custom special skull texture {} for phonk6", imagePath.getFileName());
            } else {
                USER_SKULL_TEXTURE_IDS.add(id);
                SKULL_TEXTURES.add(id);
            }
        } catch (Exception e) {
            PhonkEditMod.LOGGER.warn("Skipping custom skull image {}: {}", imagePath.getFileName(), e.getMessage());
        }
    }

    private static NativeImage scaleToSquare(NativeImage source, int size) {
        NativeImage result = new NativeImage(size, size, true);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                result.setColor(x, y, 0);
            }
        }

        double scale = Math.min((double) size / source.getWidth(), (double) size / source.getHeight());
        int scaledWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int offsetX = (size - scaledWidth) / 2;
        int offsetY = (size - scaledHeight) / 2;
        double ratioX = (double) source.getWidth() / scaledWidth;
        double ratioY = (double) source.getHeight() / scaledHeight;

        for (int y = 0; y < scaledHeight; y++) {
            int srcY = Math.min(source.getHeight() - 1, (int) Math.floor(y * ratioY));
            for (int x = 0; x < scaledWidth; x++) {
                int srcX = Math.min(source.getWidth() - 1, (int) Math.floor(x * ratioX));
                int color = source.getColor(srcX, srcY);
                result.setColor(offsetX + x, offsetY + y, color);
            }
        }

        return result;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private static String sanitizeResourceName(String input) {
        StringBuilder builder = new StringBuilder();
        String lower = input.toLowerCase(Locale.ROOT);
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        String sanitized = builder.toString().replaceAll("__+", "_");
        return sanitized.isEmpty() ? "custom" : sanitized;
    }

    private static String getFileName(Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static boolean isFreezeModeActive = false;
    private static int currentSkullIndex = 0;
    private static long worldJoinTime = 0;
    private static final long WORLD_JOIN_COOLDOWN = 3000;
    private static final long EFFECT_DELAY = 300; // 0.3 second delay
    private static long pendingEffectTime = 0;
    
    private static long freezeActivationTime = 0;
    private static final long SHAKE_DURATION = 500;
    private static final int SHAKE_INTENSITY = 8;
    private static final int MAX_BLUR_LAYERS = 8;
    private static final float SCREEN_SHAKE_RANGE = 6.0f;
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
    private static boolean wasOnGroundLastTick = true;
    private static long airStartTimeMs = 0L;
    private static boolean airTriggerConsumed = false;

    @Override
    public void onInitializeClient() {
    PhonkEditMod.LOGGER.info("Phonk Edit client initialized");
    ClientLifecycleEvents.CLIENT_STARTED.register(client -> reloadSkullTextureList());
        
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

                boolean onGround = client.player.isOnGround();
                if (onGround) {
                    airStartTimeMs = 0L;
                    airTriggerConsumed = false;
                } else {
                    if (wasOnGroundLastTick || airStartTimeMs == 0L) {
                        airStartTimeMs = System.currentTimeMillis();
                    }
                    if (ModConfig.INSTANCE.triggerOnAirTime && !airTriggerConsumed && airStartTimeMs > 0L) {
                        double thresholdMillis = ModConfig.INSTANCE.airTimeThresholdSeconds * 1000.0;
                        if (System.currentTimeMillis() - airStartTimeMs >= thresholdMillis) {
                            airTriggerConsumed = true;
                            tryTriggerEffect();
                        }
                    }
                }
                wasOnGroundLastTick = onGround;
            } else {
                lastHealthInitialized = false;
                wasBelowLowHealth = false;
                wasOnGroundLastTick = true;
                airStartTimeMs = 0L;
                airTriggerConsumed = false;
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

        long timeSinceActivation = Math.max(0L, System.currentTimeMillis() - freezeActivationTime);
        double shakeFalloff = 0.0;
        if (timeSinceActivation < SHAKE_DURATION) {
            double shakeProgress = (double) timeSinceActivation / SHAKE_DURATION;
            double easeOut = 1.0 - Math.pow(1.0 - shakeProgress, 3);
            shakeFalloff = 1.0 - easeOut;
        }

        float screenShakeOffsetY = 0.0f;
        if (shakeFalloff > 0.0) {
            float amplitude = (float) (shakeFalloff * SCREEN_SHAKE_RANGE);
            screenShakeOffsetY = (float) ((Math.random() * 2.0 - 1.0) * amplitude);
        }

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0f, screenShakeOffsetY, 0.0f);

        if (freezeTextureId != null && freezeTexWidth > 0 && freezeTexHeight > 0) {
            float sx = screenWidth / (float) Math.max(1, freezeTexWidth);
            float sy = screenHeight / (float) Math.max(1, freezeTexHeight);
            matrices.push();
            matrices.scale(sx, sy, 1.0f);
            drawContext.drawTexture(freezeTextureId, 0, 0, 0, 0, freezeTexWidth, freezeTexHeight, freezeTexWidth, freezeTexHeight);
            matrices.pop();
        }

        if (ModConfig.INSTANCE.darkenScreenDuringEffect) {
            drawContext.fill(0, 0, screenWidth, screenHeight, new Color(0, 0, 0, 110).getRGB());
        }

        matrices.pop();

        if (!ModConfig.INSTANCE.renderSkullOverlay) {
            return;
        }
        int shakeOffsetX = 0;
        int shakeOffsetY = 0;
    double blurStrength = 0.0;
    double blurEnvelope = 0.0;
    double blurRadius = 0.0;
        boolean allowShake = ModConfig.INSTANCE.skullShakeEnabled;
        boolean allowBlur = ModConfig.INSTANCE.skullBlurEnabled;

        if ((allowShake || allowBlur) && shakeFalloff > 0.0) {
            if (allowShake) {
                double shakeIntensity = shakeFalloff * SHAKE_INTENSITY * ModConfig.INSTANCE.shakeIntensity;
                shakeOffsetX = (int) ((Math.random() * 2 - 1) * shakeIntensity);
                shakeOffsetY = (int) ((Math.random() * 2 - 1) * shakeIntensity);
            }

            if (allowBlur) {
                double intensity = 5.0;
                double easePower = Math.max(0.1, ModConfig.INSTANCE.skullBlurEasePower);
                blurEnvelope = Math.pow(shakeFalloff, easePower);
                blurStrength = blurEnvelope * intensity;
                blurRadius = blurStrength * 8.0;
            }
        }

        int skullSize = Math.max(1, Math.round(SKULL_RENDER_SIZE * SKULL_RENDER_SCALE));
        int hotbarHeight = 22;
        int padding = 40;
        int y = screenHeight - hotbarHeight - skullSize - padding;
        int xCenter = screenWidth / 2 - skullSize / 2;
        Identifier skullCenter = getCurrentSkullTexture();
        int shakeXCenter = shakeOffsetX;
        int shakeYCenter = shakeOffsetY;

        if (allowBlur && blurStrength > 0.01) {
            double radius = Math.max(0.5, blurRadius);
            int blurLayers = Math.min(MAX_BLUR_LAYERS, Math.max(2, (int) Math.ceil(radius)));
            double sigma = radius / 2.0;
            double twoSigmaSq = 2.0 * sigma * sigma;
            double weightSum = 0.0;
            double[] weights = new double[blurLayers];
            for (int i = 0; i < blurLayers; i++) {
                double offset = i + 1;
                double weight = Math.exp(-(offset * offset) / twoSigmaSq);
                weights[i] = weight;
                weightSum += weight * 2.0;
            }
            float alphaScale = (float) Math.min(1.0, blurEnvelope);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (int i = 0; i < blurLayers; i++) {
                double offset = i + 1;
                double weight = weights[i] / weightSum;
                float layerAlpha = (float) (alphaScale * weight * 2.0);
                float blurOffset = (float) (offset * radius / blurLayers);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, layerAlpha);
                int drawX = Math.round(xCenter + shakeXCenter + blurOffset);
                int drawXMirror = Math.round(xCenter + shakeXCenter - blurOffset);
                drawScaledSkull(drawContext, skullCenter, drawX, y + shakeYCenter, skullSize);
                drawScaledSkull(drawContext, skullCenter, drawXMirror, y + shakeYCenter, skullSize);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }

        drawScaledSkull(drawContext, skullCenter, xCenter + shakeXCenter, y + shakeYCenter, skullSize);
    }

    private static void drawScaledSkull(DrawContext drawContext, Identifier texture, int x, int y, int size) {
        float scale = size / (float) SKULL_TEXTURE_SIZE;
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        drawContext.drawTexture(texture, 0, 0, 0, 0, SKULL_TEXTURE_SIZE, SKULL_TEXTURE_SIZE, SKULL_TEXTURE_SIZE, SKULL_TEXTURE_SIZE);
        matrices.pop();
    }

    public static void renderTopLayer(DrawContext drawContext) {
        if (!isFreezeModeActive || !ModConfig.INSTANCE.showCinematicBars) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float targetAspect = 9f / 16f;
        float currentAspect = screenWidth / (float) screenHeight;
        if (Math.abs(currentAspect - targetAspect) <= 0.001f) {
            return;
        }

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0f, 0.0f, 1000.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    public static void activateFreezeEffect() {
        isFreezeModeActive = true;
        freezeActivationTime = System.currentTimeMillis();
        overrideSkullTexture = null;
        currentSkullIndex = 0;
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

        PhonkManager manager = PhonkManager.getInstance();
        manager.playRandomTrack();
        if (manager.getCurrentSoundEvent() == ModSounds.PHONK6) {
            overrideSkullTexture = currentSpecialSkullTexture;
        } else if (!SKULL_TEXTURES.isEmpty()) {
            currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.size());
        }
        requestCapture = true;
        PhonkEditMod.LOGGER.info("Activated freeze effect");
    }

    public static void endFreezeEffect() {
        isFreezeModeActive = false;
        overrideSkullTexture = null;
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
        if (overrideSkullTexture != null) {
            return overrideSkullTexture;
        }
        if (SKULL_TEXTURES.isEmpty()) {
            return BUILTIN_SKULL_TEXTURES[0];
        }
        int safeIndex = Math.min(currentSkullIndex, Math.max(0, SKULL_TEXTURES.size() - 1));
        return SKULL_TEXTURES.get(safeIndex);
    }

    public static void onWorldJoin() {
    worldJoinTime = System.currentTimeMillis();
    pendingEffectTime = 0;
    hasPreservedVelocity = false;
    preservedVelocity = Vec3d.ZERO;
    preservedSprinting = false;
    wasOnGroundLastTick = true;
    airStartTimeMs = 0L;
    airTriggerConsumed = false;
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

    public static void onBlockPlaced() {
        if (!ModConfig.INSTANCE.triggerOnBlockPlace) {
            return;
        }
        scheduleEffect(true);
    }

    private static void scheduleEffect() {
        scheduleEffect(false);
    }

    private static void scheduleEffect(boolean force) {
        if (!ModConfig.INSTANCE.enablePhonkEffect) return;
        if (!canTrigger()) return;
        if (isFreezeModeActive) return;
        if (pendingEffectTime > 0) return; // Already scheduled
        if (!force && Math.random() > ModConfig.INSTANCE.triggerChance) {
            return;
        }
        pendingEffectTime = System.currentTimeMillis() + EFFECT_DELAY;
    }

    private static void tryTriggerEffect() {
        scheduleEffect(false);
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
