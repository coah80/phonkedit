package com.phonkedit.client;

import com.phonkedit.ModSounds;
import com.phonkedit.PhonkEditMod;
import com.phonkedit.audio.PhonkManager;
import com.phonkedit.audio.CustomSongs;
import com.phonkedit.config.ModConfig;
import com.phonkedit.network.NetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

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
    private static final List<CustomTexture> USER_SKULL_TEXTURES = new ArrayList<>();
    private static final Path USER_CONFIG_DIR = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("phonkedit");
    private static final Path USER_IMAGES_DIR = USER_CONFIG_DIR.resolve("images");
    private static final String DEFAULT_EXPORT_MARKER = ".defaults_copied";
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "wbmp", "tif", "tiff");
    private static final int SKULL_TEXTURE_SIZE = 256;
    private static final int SKULL_RENDER_SIZE = 256;
    private static int userSkullIdCounter = 0;
    private static int specialSkullIdCounter = 0;
    private static CustomTexture customSpecialSkull = null;
    private static Identifier currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
    private static Identifier overrideSkullTexture = null;
    private static boolean curseBroken = false;
    private static boolean wasUsingItemLastTick = false;
    private static boolean wasSleepingLastTick = false;
    private static boolean ridingTargetVehicleLastTick = false;

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
        currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
        overrideSkullTexture = null;

        try {
            Files.createDirectories(USER_CONFIG_DIR);
            Files.createDirectories(USER_IMAGES_DIR);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.error("Unable to prepare skull image directory {}", USER_IMAGES_DIR, e);
            currentSkullIndex = Math.min(currentSkullIndex, Math.max(0, SKULL_TEXTURES.size() - 1));
            return;
        }

        exportDefaultImagesIfNeeded(resourceManager);
        loadCustomSkulls(textureManager);

        if (SKULL_TEXTURES.isEmpty()) {
            Collections.addAll(SKULL_TEXTURES, BUILTIN_SKULL_TEXTURES);
        }
        updateSpecialSkullSelection(resourceManager);

        currentSkullIndex = Math.min(currentSkullIndex, Math.max(0, SKULL_TEXTURES.size() - 1));

        int userTextureCount = USER_SKULL_TEXTURES.size() + (customSpecialSkull != null ? 1 : 0);
        int totalTextures = SKULL_TEXTURES.size() + (customSpecialSkull != null ? 1 : 0);
        PhonkEditMod.LOGGER.info("Loaded {} skull textures ({} from user directory)", totalTextures, userTextureCount);
        if (userTextureCount > 0) {
            PhonkEditMod.LOGGER.info("Custom skull directory: {}", USER_IMAGES_DIR.toAbsolutePath());
        }
    }

    private static void clearUserSkullTextures(TextureManager textureManager) {
        if (!USER_SKULL_TEXTURES.isEmpty()) {
            for (CustomTexture texture : USER_SKULL_TEXTURES) {
                texture.destroy(textureManager);
            }
            USER_SKULL_TEXTURES.clear();
        }
        if (customSpecialSkull != null) {
            customSpecialSkull.destroy(textureManager);
            customSpecialSkull = null;
        }
        currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
        userSkullIdCounter = 0;
        specialSkullIdCounter = 0;
        overrideSkullTexture = null;
    }

    private static void exportDefaultImagesIfNeeded(ResourceManager resourceManager) {
        Path markerInConfig = USER_CONFIG_DIR.resolve(DEFAULT_EXPORT_MARKER);
        Path markerInImages = USER_IMAGES_DIR.resolve(DEFAULT_EXPORT_MARKER); // legacy location used by earlier builds
        if (Files.exists(markerInConfig) || Files.exists(markerInImages)) {
            return;
        }
        exportBuiltinSkulls(resourceManager);
        exportSpecialSkull(resourceManager);
        try {
            Files.createFile(markerInConfig);
        } catch (IOException e) {
            PhonkEditMod.LOGGER.debug("Unable to record default export marker {}: {}", markerInConfig, e.getMessage());
        }
    }

    private static void updateSpecialSkullSelection(ResourceManager resourceManager) {
        if (customSpecialSkull != null) {
            currentSpecialSkullTexture = customSpecialSkull.id();
            return;
        }
        boolean hasBuiltinSpecial = resourceManager != null && resourceManager.getResource(SPECIAL_SKULL_PHONK6).isPresent();
        if (hasBuiltinSpecial) {
            currentSpecialSkullTexture = SPECIAL_SKULL_PHONK6;
        } else {
            currentSpecialSkullTexture = null;
        }
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
                        String extension = getFileExtension(lower);
                        return extension != null && SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
                    })
                    .sorted()
                    .forEach(path -> {
                        String baseName = stripExtension(path.getFileName().toString());
                        boolean special = isSpecialName(baseName);
                        if (special && customSpecialSkull != null) {
                            return;
                        }
                        registerCustomSkull(path, textureManager, special);
                    });
        } catch (IOException e) {
            PhonkEditMod.LOGGER.warn("Failed to scan custom skulls in {}", USER_IMAGES_DIR, e);
        }
    }

    private static void registerCustomSkull(Path imagePath, TextureManager textureManager, boolean special) {
        LoadedImageData imageData;
        try {
            imageData = loadCustomImageData(imagePath);
        } catch (Exception e) {
            PhonkEditMod.LOGGER.warn("Skipping custom skull image {}: {}", imagePath.getFileName(), e.getMessage());
            return;
        }

        List<NativeImage> frames = new ArrayList<>(imageData.frames());
        List<Integer> frameDurations = new ArrayList<>(imageData.frameDurations());
        boolean animated = frames.size() > 1;

        String baseName = stripExtension(imagePath.getFileName().toString());
        String sanitized = sanitizeResourceName(baseName);
        int suffix = special ? specialSkullIdCounter : userSkullIdCounter;
        String texturePath = special
            ? "user/skull_special/" + sanitized + "_" + suffix
            : "user/skull/" + sanitized + "_" + suffix;
        Identifier id = Identifier.of(PhonkEditMod.MOD_ID, texturePath);

        NativeImageBackedTexture texture = null;
        GifAnimation animation = null;

        try {
            NativeImage initialFrameCopy = copyImage(frames.get(0));
            texture = new NativeImageBackedTexture(initialFrameCopy);
            textureManager.registerTexture(id, texture);
            texture.upload();

            if (animated) {
                animation = new GifAnimation(frames, frameDurations);
                animation.reset(texture);
            } else {
                for (NativeImage frame : frames) {
                    frame.close();
                }
                frames.clear();
            }

            if (special) {
                specialSkullIdCounter++;
            } else {
                userSkullIdCounter++;
            }

            CustomTexture handle = new CustomTexture(id, texture, animation, imagePath);
            if (special) {
                customSpecialSkull = handle;
                currentSpecialSkullTexture = id;
                PhonkEditMod.LOGGER.info("Registered custom special skull texture {}", imagePath.getFileName());
            } else {
                USER_SKULL_TEXTURES.add(handle);
                SKULL_TEXTURES.add(id);
                PhonkEditMod.LOGGER.debug("Registered custom skull texture {}", imagePath.getFileName());
            }
        } catch (Exception e) {
            if (animation != null) {
                animation.close();
            } else if (!frames.isEmpty()) {
                for (NativeImage frame : frames) {
                    try {
                        frame.close();
                    } catch (Exception ignored) {
                        // Ignored
                    }
                }
                frames.clear();
            }

            if (texture != null) {
                textureManager.destroyTexture(id);
                try {
                    texture.close();
                } catch (Exception ignored) {
                }
            }

            PhonkEditMod.LOGGER.warn("Skipping custom skull image {}: {}", imagePath.getFileName(), e.getMessage());
        }
    }

    private static String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isSpecialName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.equals("skull20");
    }

    private static LoadedImageData loadCustomImageData(Path imagePath) throws IOException {
        String filename = imagePath.getFileName().toString();
        String extension = getFileExtension(filename);
        if ("gif".equals(extension)) {
            return loadGif(imagePath);
        }
        try (InputStream inputStream = Files.newInputStream(imagePath)) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new IOException("Unsupported image format");
            }
            NativeImage nativeImage = bufferedToNative(bufferedImage);
            try {
                NativeImage scaled = scaleToSquare(nativeImage, SKULL_TEXTURE_SIZE);
                return new LoadedImageData(Collections.singletonList(scaled), Collections.singletonList(Integer.MAX_VALUE));
            } finally {
                nativeImage.close();
            }
        }
    }

    private static LoadedImageData loadGif(Path imagePath) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("gif");
        if (!readers.hasNext()) {
            throw new IOException("GIF support is unavailable");
        }
        ImageReader reader = readers.next();
        try (ImageInputStream stream = ImageIO.createImageInputStream(Files.newInputStream(imagePath))) {
            reader.setInput(stream, false, false);

            int canvasW = -1, canvasH = -1;
            try {
                IIOMetadata streamMeta = reader.getStreamMetadata();
                int[] wh = parseGifLogicalScreenSize(streamMeta);
                canvasW = wh[0];
                canvasH = wh[1];
            } catch (Exception ignored) { }

            int frameCount = Math.max(1, reader.getNumImages(true));
            if (canvasW <= 0 || canvasH <= 0) {
                BufferedImage first = reader.read(0);
                canvasW = first.getWidth();
                canvasH = first.getHeight();
            }

            BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = canvas.createGraphics();
            g.setComposite(java.awt.AlphaComposite.SrcOver);

            List<NativeImage> frames = new ArrayList<>(frameCount);
            List<Integer> durations = new ArrayList<>(frameCount);

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frameImg = reader.read(i);
                IIOMetadata meta = reader.getImageMetadata(i);
                int delay = extractGifDelay(meta);
                GifFrameInfo info = parseGifFrameInfo(meta, frameImg);

                g.drawImage(frameImg, info.left, info.top, null);

                BufferedImage snapshot = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2 = snapshot.createGraphics();
                g2.drawImage(canvas, 0, 0, null);
                g2.dispose();

                NativeImage nativeComposed = bufferedToNative(snapshot);
                try {
                    NativeImage scaled = scaleToSquare(nativeComposed, SKULL_TEXTURE_SIZE);
                    frames.add(scaled);
                } finally {
                    nativeComposed.close();
                }
                durations.add(delay);

                if ("restoreToBackgroundColor".equals(info.disposal)) {
                    java.awt.Composite old = g.getComposite();
                    g.setComposite(java.awt.AlphaComposite.Clear);
                    g.fillRect(info.left, info.top, info.width, info.height);
                    g.setComposite(old);
                } else if ("restoreToPrevious".equals(info.disposal)) {
                    java.awt.Composite old = g.getComposite();
                    g.setComposite(java.awt.AlphaComposite.Clear);
                    g.fillRect(info.left, info.top, info.width, info.height);
                    g.setComposite(old);
                }
            }

            g.dispose();

            if (frames.isEmpty()) {
                throw new IOException("GIF contains no readable frames");
            }
            while (durations.size() < frames.size()) {
                durations.add(100);
            }
            return new LoadedImageData(frames, durations);
        } finally {
            reader.dispose();
        }
    }

    private static int extractGifDelay(IIOMetadata metadata) {
        int delayCs = 10;
        if (metadata != null) {
            try {
                String format = metadata.getNativeMetadataFormatName();
                Node tree = metadata.getAsTree(format);
                int parsed = findGifDelay(tree);
                if (parsed >= 0) {
                    delayCs = parsed;
                }
            } catch (Exception ignored) {
            }
        }
        int delayMs = Math.max(1, delayCs) * 10;
        return Math.max(20, delayMs);
    }

    private static int findGifDelay(Node node) {
        if (node == null) {
            return -1;
        }
        if ("GraphicControlExtension".equals(node.getNodeName())) {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node delayNode = attributes.getNamedItem("delayTime");
                if (delayNode != null) {
                    try {
                        return Integer.parseInt(delayNode.getNodeValue());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            int delay = findGifDelay(child);
            if (delay >= 0) {
                return delay;
            }
        }
        return -1;
    }

    private static int[] parseGifLogicalScreenSize(IIOMetadata streamMeta) {
        int w = -1, h = -1;
        if (streamMeta != null) {
            try {
                String format = streamMeta.getNativeMetadataFormatName();
                Node root = streamMeta.getAsTree(format);
                for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if ("LogicalScreenDescriptor".equals(n.getNodeName())) {
                        NamedNodeMap at = n.getAttributes();
                        w = parseIntAttr(at, "logicalScreenWidth", -1);
                        h = parseIntAttr(at, "logicalScreenHeight", -1);
                        break;
                    }
                }
            } catch (Exception ignored) { }
        }
        return new int[] { w, h };
    }

    private static class GifFrameInfo {
        final int left, top, width, height;
        final String disposal;
        GifFrameInfo(int left, int top, int width, int height, String disposal) {
            this.left = left; this.top = top; this.width = width; this.height = height; this.disposal = disposal;
        }
    }

    private static GifFrameInfo parseGifFrameInfo(IIOMetadata meta, BufferedImage img) {
        int left = 0, top = 0, w = img.getWidth(), h = img.getHeight();
        String disposal = "none";
        if (meta != null) {
            try {
                String format = meta.getNativeMetadataFormatName();
                Node root = meta.getAsTree(format);
                for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if ("ImageDescriptor".equals(n.getNodeName())) {
                        NamedNodeMap at = n.getAttributes();
                        left = parseIntAttr(at, "imageLeftPosition", left);
                        top = parseIntAttr(at, "imageTopPosition", top);
                        w = parseIntAttr(at, "imageWidth", w);
                        h = parseIntAttr(at, "imageHeight", h);
                    } else if ("GraphicControlExtension".equals(n.getNodeName())) {
                        NamedNodeMap at = n.getAttributes();
                        Node disp = at.getNamedItem("disposalMethod");
                        if (disp != null) disposal = disp.getNodeValue();
                    }
                }
            } catch (Exception ignored) { }
        }
        return new GifFrameInfo(left, top, w, h, disposal);
    }

    private static int parseIntAttr(NamedNodeMap at, String name, int def) {
        if (at == null) return def;
        Node n = at.getNamedItem(name);
        if (n == null) return def;
        try { return Integer.parseInt(n.getNodeValue()); } catch (Exception e) { return def; }
    }

    private static NativeImage bufferedToNative(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setColor(x, y, abgr);
            }
        }
        return nativeImage;
    }

    private static NativeImage copyImage(NativeImage source) {
        NativeImage copy = new NativeImage(source.getWidth(), source.getHeight(), true);
        copyImageData(source, copy);
        return copy;
    }

    private static void copyImageData(NativeImage source, NativeImage target) {
        int width = Math.min(source.getWidth(), target.getWidth());
        int height = Math.min(source.getHeight(), target.getHeight());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                target.setColor(x, y, source.getColor(x, y));
            }
        }
    }

    private static final class LoadedImageData {
        private final List<NativeImage> frames;
        private final List<Integer> frameDurations;

        private LoadedImageData(List<NativeImage> frames, List<Integer> durations) {
            if (frames == null || frames.isEmpty()) {
                throw new IllegalArgumentException("frames must not be empty");
            }
            this.frames = List.copyOf(frames);
            if (durations == null || durations.isEmpty()) {
                durations = Collections.nCopies(this.frames.size(), 100);
            } else if (durations.size() < this.frames.size()) {
                List<Integer> copy = new ArrayList<>(durations);
                while (copy.size() < this.frames.size()) {
                    copy.add(100);
                }
                durations = copy;
            }
            this.frameDurations = List.copyOf(durations);
        }

        private List<NativeImage> frames() {
            return frames;
        }

        private List<Integer> frameDurations() {
            return frameDurations;
        }
    }

    private static void tickCustomTextureAnimations() {
        if (!isFreezeModeActive) {
            return;
        }
        Identifier activeTexture = overrideSkullTexture != null ? overrideSkullTexture : getCurrentSkullTexture();
        CustomTexture active = findCustomTexture(activeTexture);
        if (active != null) {
            active.tick();
        }
    }

    private static CustomTexture findCustomTexture(Identifier textureId) {
        if (textureId == null) {
            return null;
        }
        if (customSpecialSkull != null && customSpecialSkull.id().equals(textureId)) {
            return customSpecialSkull;
        }
        for (CustomTexture texture : USER_SKULL_TEXTURES) {
            if (texture.id().equals(textureId)) {
                return texture;
            }
        }
        return null;
    }

    private static void resetAnimationFor(Identifier textureId) {
        CustomTexture texture = findCustomTexture(textureId);
        if (texture != null) {
            texture.resetAnimation();
        }
    }

    private static Identifier resolveSpecialSkullTexture() {
        if (currentSpecialSkullTexture != null) {
            return currentSpecialSkullTexture;
        }
        if (customSpecialSkull != null) {
            return customSpecialSkull.id();
        }
        if (!USER_SKULL_TEXTURES.isEmpty()) {
            CustomTexture random = USER_SKULL_TEXTURES.get((int) (Math.random() * USER_SKULL_TEXTURES.size()));
            return random.id();
        }
        if (!SKULL_TEXTURES.isEmpty()) {
            return SKULL_TEXTURES.get((int) (Math.random() * SKULL_TEXTURES.size()));
        }
        return BUILTIN_SKULL_TEXTURES.length > 0 ? BUILTIN_SKULL_TEXTURES[0] : null;
    }

    private static boolean isSpecialTrack(SoundEvent event) {
        return event == ModSounds.PHONK6;
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
    private static final long EFFECT_DELAY = 300;
    private static long pendingEffectTime = 0;
    private static final long TRACK_PLAY_GRACE_MS = 600;
    private static long trackPlayWaitDeadline = 0;
    
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
    private static boolean wasEatingFoodLastTick = false;

    @Override
    public void onInitializeClient() {
    NetworkHandler.initClient(payload -> onActivateFromNetwork(payload.soundId(), payload.pitch(), payload.imagePng().orElse(null)), payload -> onCurseStatus(payload.curseBroken()), payload -> onTriggerSuggestion(payload.reason()));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of("phonkedit", "custom_songs_loader");
            }

            @Override
            public void reload(net.minecraft.resource.ResourceManager manager) {
                    CustomSongs.prepareAndReload(manager);
            }
        });
            ClientLifecycleEvents.CLIENT_STARTED.register(client -> CustomSongs.initializeOnClientStart());
    PhonkEditMod.LOGGER.info("Phonk Edit client initialized");
    ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            reloadSkullTextureList();
            CustomSongs.initializeOnClientStart();
        });
        
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            renderOverlayIfActive(drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ModConfig.INSTANCE.devDisablePauseOnLostFocus) {
                try {
                    if (client != null && client.options != null) {
                        if (client.options.pauseOnLostFocus) {
                            client.options.pauseOnLostFocus = false;
                        }
                    }
                } catch (Throwable ignored) { }
            }

            tickCustomTextureAnimations();
            
            if (isFreezeModeActive && client.isPaused() && !ModConfig.INSTANCE.devDontEndOnPause) {
                endFreezeEffect();
            }

            if (pendingEffectTime > 0 && System.currentTimeMillis() >= pendingEffectTime) {
                pendingEffectTime = 0;
                triggerSyncedOrLocal();
            }
            
            if (isFreezeModeActive) {
                boolean trackKnown = PhonkManager.getInstance().isPlaying();
                boolean stillPlaying = PhonkManager.getInstance().isCurrentTrackPlaying();
                if (trackKnown) {
                    if (!stillPlaying) {
                        if (trackPlayWaitDeadline == 0 || System.currentTimeMillis() >= trackPlayWaitDeadline) {
                            endFreezeEffect();
                        }
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

                boolean usingItem = client.player.isUsingItem();
                boolean eatingThisTick = false;
                if (usingItem) {
                    Hand hand = client.player.getActiveHand();
                    if (hand != null) {
                        ItemStack stack = client.player.getStackInHand(hand);
                        if (!stack.isEmpty() && stack.getComponents().contains(DataComponentTypes.FOOD)) {
                            eatingThisTick = true;
                        }
                    }
                }
                if (!usingItem && wasUsingItemLastTick && wasEatingFoodLastTick) {
                    onFoodStarted();
                }
                wasEatingFoodLastTick = eatingThisTick;
                wasUsingItemLastTick = usingItem;

                boolean sleeping = client.player.isSleeping();
                if (sleeping && !wasSleepingLastTick) {
                    onBedUsed();
                }
                wasSleepingLastTick = sleeping;

                boolean ridingTarget = false;
                Entity vehicle = client.player.getVehicle();
                if (vehicle instanceof BoatEntity || vehicle instanceof AbstractMinecartEntity) {
                    ridingTarget = true;
                }
                if (ridingTarget && !ridingTargetVehicleLastTick) {
                    onVehicleMounted();
                }
                ridingTargetVehicleLastTick = ridingTarget;
            } else {
                lastHealthInitialized = false;
                wasBelowLowHealth = false;
                wasOnGroundLastTick = true;
                airStartTimeMs = 0L;
                airTriggerConsumed = false;
                wasUsingItemLastTick = false;
                wasSleepingLastTick = false;
                ridingTargetVehicleLastTick = false;
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onWorldJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (isFreezeModeActive) {
                endFreezeEffect();
            }
            pendingEffectTime = 0;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onWorldJoin();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
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

    int skullBaseSize = Math.max(1, Math.round(SKULL_RENDER_SIZE * (float)Math.max(0.1, Math.min(2.0, ModConfig.INSTANCE.skullScale))));
    double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
    double targetGuiScale = 3.0;
    int skullSize = Math.max(1, (int)Math.round(skullBaseSize * (targetGuiScale / Math.max(1.0, scaleFactor))));
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
        SoundEvent currentEvent = manager.getCurrentSoundEvent();
        Identifier selectedTexture = null;

    if (isSpecialTrack(currentEvent)) {
            Identifier specialTexture = resolveSpecialSkullTexture();
            if (specialTexture != null) {
                overrideSkullTexture = specialTexture;
                selectedTexture = specialTexture;
            } else if (!SKULL_TEXTURES.isEmpty()) {
                currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.size());
                selectedTexture = SKULL_TEXTURES.get(currentSkullIndex);
            } else {
                overrideSkullTexture = SPECIAL_SKULL_PHONK6;
                selectedTexture = overrideSkullTexture;
            }
        } else if (!SKULL_TEXTURES.isEmpty()) {
            currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.size());
            selectedTexture = SKULL_TEXTURES.get(currentSkullIndex);
        } else {
            overrideSkullTexture = SPECIAL_SKULL_PHONK6;
            selectedTexture = overrideSkullTexture;
        }

        if (selectedTexture != null) {
            resetAnimationFor(selectedTexture);
        }

        if (manager.isPlaying() && !manager.isCurrentTrackPlaying()) {
            trackPlayWaitDeadline = System.currentTimeMillis() + TRACK_PLAY_GRACE_MS;
        } else {
            trackPlayWaitDeadline = 0;
        }

        requestCapture = true;
        PhonkEditMod.LOGGER.info("Activated freeze effect");
    }

    private static void triggerSyncedOrLocal() {
        if (curseBroken) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean canNet = mc != null && mc.getNetworkHandler() != null;
        if (canNet) {
            var sel = PhonkManager.getInstance().pickRandomTrackAndPitch();
            Identifier chosenTexture = chooseSkullTextureForEvent(sel.event);
            byte[] skullPng = encodeSkullTextureToPng(chosenTexture);
            NetworkHandler.sendActivateRequestToServer(getSoundId(sel.event), sel.pitch, skullPng);
        } else {
            activateFreezeEffect();
        }
    }

    private static Identifier chooseSkullTextureForEvent(SoundEvent currentEvent) {
        Identifier selectedTexture = null;
        if (isSpecialTrack(currentEvent)) {
            Identifier specialTexture = resolveSpecialSkullTexture();
            if (specialTexture != null) {
                selectedTexture = specialTexture;
            } else if (!SKULL_TEXTURES.isEmpty()) {
                currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.size());
                selectedTexture = SKULL_TEXTURES.get(currentSkullIndex);
            } else {
                selectedTexture = SPECIAL_SKULL_PHONK6;
            }
        } else if (!SKULL_TEXTURES.isEmpty()) {
            currentSkullIndex = (int) (Math.random() * SKULL_TEXTURES.size());
            selectedTexture = SKULL_TEXTURES.get(currentSkullIndex);
        } else {
            selectedTexture = SPECIAL_SKULL_PHONK6;
        }
        return selectedTexture;
    }

    private static String getSoundId(SoundEvent event) {
        var id = net.minecraft.registry.Registries.SOUND_EVENT.getId(event);
        return id != null ? id.toString() : "phonkedit:phonk1";
    }

    private static byte[] encodeSkullTextureToPng(Identifier id) {
        try {
            ResourceManager rm = MinecraftClient.getInstance().getResourceManager();
            var opt = rm.getResource(id);
            if (opt.isPresent()) {
                try (InputStream in = opt.get().getInputStream()) {
                    return in.readAllBytes();
                }
            }
        } catch (Exception ignored) {}

        MinecraftClient mc = MinecraftClient.getInstance();
        TextureManager tm = mc.getTextureManager();
        var tex = tm.getTexture(id);
        if (tex instanceof NativeImageBackedTexture nibt) {
            NativeImage img = nibt.getImage();
            if (img != null) {
                try {
                    java.nio.file.Path tmp = java.nio.file.Files.createTempFile("phonk-skull", ".png");
                    img.writeTo(tmp.toFile());
                    byte[] bytes = java.nio.file.Files.readAllBytes(tmp);
                    java.nio.file.Files.deleteIfExists(tmp);
                    return bytes;
                } catch (Exception e) {
                    PhonkEditMod.LOGGER.warn("Failed to encode skull texture to PNG: {}", e.toString());
                }
            }
        }
        return null;
    }

    public static void onActivateFromNetwork(String soundId, float pitch, byte[] skullPng) {
        Identifier dyn = null;
        if (skullPng != null && skullPng.length > 0) {
            dyn = registerDynamicSkull(skullPng);
        }
        isFreezeModeActive = true;
        freezeActivationTime = System.currentTimeMillis();
        overrideSkullTexture = dyn;
        currentSkullIndex = 0;

        PhonkManager manager = PhonkManager.getInstance();
        Identifier sid = Identifier.tryParse(soundId);
        if (sid != null) {
            manager.playTrackById(sid, pitch);
        } else {
            manager.playRandomTrack();
        }

        if (overrideSkullTexture != null) {
            resetAnimationFor(overrideSkullTexture);
        }
        if (manager.isPlaying() && !manager.isCurrentTrackPlaying()) {
            trackPlayWaitDeadline = System.currentTimeMillis() + TRACK_PLAY_GRACE_MS;
        } else {
            trackPlayWaitDeadline = 0;
        }
        requestCapture = true;
        PhonkEditMod.LOGGER.info("Activated freeze effect (synced)");
    }

    private static Identifier registerDynamicSkull(byte[] png) {
        try {
            NativeImage img = NativeImage.read(new java.io.ByteArrayInputStream(png));
            NativeImage scaled = scaleToSquare(img, SKULL_TEXTURE_SIZE);
            img.close();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(scaled);
            Identifier id = Identifier.of(PhonkEditMod.MOD_ID, "net/skull_" + System.currentTimeMillis());
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
            return id;
        } catch (Exception e) {
            PhonkEditMod.LOGGER.warn("Failed to register dynamic skull from PNG: {}", e.toString());
            return null;
        }
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
        // Notify server to end any protection/pause windows tied to the effect
        try {
            NetworkHandler.sendEndEffectToServer();
        } catch (Throwable ignored) {}
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
        curseBroken = false;
        hasPreservedVelocity = false;
        preservedVelocity = Vec3d.ZERO;
        preservedSprinting = false;
        wasOnGroundLastTick = true;
        airStartTimeMs = 0L;
        airTriggerConsumed = false;
        wasUsingItemLastTick = false;
        wasSleepingLastTick = false;
        ridingTargetVehicleLastTick = false;
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
        scheduleEffect(false);
    }

    public static void onLeverUsed() {
        if (!ModConfig.INSTANCE.triggerOnLeverUse) {
            return;
        }
        scheduleEffect(false);
    }

    public static void onDoorUsed() {
        if (!ModConfig.INSTANCE.triggerOnDoorUse) {
            return;
        }
        scheduleEffect(false);
    }

    public static void onVehicleMounted() {
        if (!ModConfig.INSTANCE.triggerOnVehicleMount) {
            return;
        }
        scheduleEffect(false);
    }

    public static void onFoodStarted() {
        if (!ModConfig.INSTANCE.triggerOnEatFood) {
            return;
        }
        scheduleEffect(false);
    }

    public static void onBedUsed() {
        if (!ModConfig.INSTANCE.triggerOnUseBed) {
            return;
        }
        scheduleEffect(false);
    }

    private static void scheduleEffect() {
        scheduleEffect(false);
    }

    private static void scheduleEffect(boolean force) {
        if (curseBroken) return;
        if (!ModConfig.INSTANCE.enablePhonkEffect) return;
        if (!canTrigger()) return;
        if (isFreezeModeActive) return;
        if (pendingEffectTime > 0) return;
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
                image = new NativeImage(w, h, false);
                fb.beginRead();
                image.loadFromTextureImage(0, false);
                fb.endRead();
            }

            int half = h / 2;
            for (int y = 0; y < half; y++) {
                int opposite = h - 1 - y;
                for (int x = 0; x < w; x++) {
                    int c1 = image.getColor(x, y);
                    int c2 = image.getColor(x, opposite);
                    image.setColor(x, y, c2);
                    image.setColor(x, opposite, c1);
                }
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

    private static void onCurseStatus(boolean broken) {
        curseBroken = broken;
        if (broken) {
            pendingEffectTime = 0;
        }
    }

    private static void onTriggerSuggestion(String reason) {
        if (curseBroken) {
            return;
        }
        boolean allowed = switch (reason) {
            case "boss:dragon" -> ModConfig.INSTANCE.triggerOnDragonKill;
            case "boss:wither" -> ModConfig.INSTANCE.triggerOnWitherKill;
            case "boss:warden" -> ModConfig.INSTANCE.triggerOnWardenKill;
            case "boss:elder_guardian" -> ModConfig.INSTANCE.triggerOnElderGuardianKill;
            default -> false;
        };
        if (!allowed) {
            return;
        }
        scheduleEffect(false);
    }

    private static final class CustomTexture {
        private final Identifier id;
        private final NativeImageBackedTexture texture;
        private final GifAnimation animation;
        private final Path sourcePath;

        private CustomTexture(Identifier id, NativeImageBackedTexture texture, GifAnimation animation, Path sourcePath) {
            this.id = id;
            this.texture = texture;
            this.animation = animation;
            this.sourcePath = sourcePath;
        }

        private Identifier id() {
            return id;
        }

        private void tick() {
            if (animation != null) {
                animation.tick(texture);
            }
        }

        private void resetAnimation() {
            if (animation != null) {
                animation.reset(texture);
            }
        }

        private void destroy(TextureManager textureManager) {
            if (textureManager != null) {
                textureManager.destroyTexture(id);
            }
            if (animation != null) {
                animation.close();
            }
            try {
                texture.close();
            } catch (Exception ignored) {
                // Ignored
            }
        }

        @Override
        public String toString() {
            return "CustomTexture{" + id + " from " + sourcePath.getFileName() + "}";
        }
    }

    private static final class GifAnimation implements AutoCloseable {
        private final List<NativeImage> frames;
        private final int[] frameDurations;
        private int frameIndex = 0;
        private long frameStartTime = System.currentTimeMillis();

        private GifAnimation(List<NativeImage> frames, List<Integer> durations) {
            this.frames = new ArrayList<>(frames);
            this.frameDurations = new int[this.frames.size()];
            for (int i = 0; i < this.frames.size(); i++) {
                int duration = i < durations.size() ? durations.get(i) : durations.isEmpty() ? 100 : durations.get(durations.size() - 1);
                this.frameDurations[i] = Math.max(20, duration);
            }
        }

        private void reset(NativeImageBackedTexture texture) {
            frameIndex = 0;
            frameStartTime = System.currentTimeMillis();
            apply(texture);
        }

        private void tick(NativeImageBackedTexture texture) {
            if (frames.size() <= 1) {
                return;
            }
            long now = System.currentTimeMillis();
            int duration = frameDurations[frameIndex];
            if (now - frameStartTime >= duration) {
                frameIndex = (frameIndex + 1) % frames.size();
                frameStartTime = now;
                apply(texture);
            }
        }

        private void apply(NativeImageBackedTexture texture) {
            NativeImage target = texture.getImage();
            if (target == null) {
                return;
            }
            copyImageData(frames.get(frameIndex), target);
            texture.upload();
        }

        @Override
        public void close() {
            for (NativeImage frame : frames) {
                frame.close();
            }
        }
    }

    
}
