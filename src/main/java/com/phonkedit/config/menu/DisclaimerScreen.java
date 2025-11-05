package com.phonkedit.config.menu;

import com.phonkedit.PhonkEditMod;
import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Lightweight prompt shown when Mod Menu is missing so users know how to reach the config.
 */
public class DisclaimerScreen extends Screen {
    private static final Text TITLE = Text.literal("Phonk Edit – Mod Menu Recommended");
    private static final Text BODY_ONE = Text.literal("Mod Menu provides the full in-game configuration screen.");
    private static final Text BODY_TWO = Text.literal("Install it for easier setup, or continue without it.");
    private static final Text BODY_THREE = Text.literal("This message only appears once.");
    private static final Text DOWNLOAD_BUTTON = Text.literal("Open Mod Menu Download Page");
    private static final Text CONTINUE_BUTTON = Text.literal("Continue Without Mod Menu");
    private static final String MOD_MENU_URL = "https://modrinth.com/mod/modmenu";

    private final Screen parent;

    public DisclaimerScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 + 10;
        addDrawableChild(ButtonWidget.builder(DOWNLOAD_BUTTON, button -> openModMenuPage())
            .dimensions(centerX - 155, startY, 310, 20)
            .build());
        addDrawableChild(ButtonWidget.builder(CONTINUE_BUTTON, button -> continueToParent())
            .dimensions(centerX - 155, startY + 24, 310, 20)
            .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int y = this.height / 2 - 55;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, y, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, BODY_ONE, centerX, y + 20, 0xA0A0A0);
        context.drawCenteredTextWithShadow(this.textRenderer, BODY_TWO, centerX, y + 35, 0xA0A0A0);
        context.drawCenteredTextWithShadow(this.textRenderer, BODY_THREE, centerX, y + 50, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        continueToParent();
    }

    private void continueToParent() {
        ModConfig.INSTANCE.modMenuDisclaimerShown = true;
        ModConfig.save();
        PhonkEditClient.skipNextDisclaimerCheck = true;
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private void openModMenuPage() {
        try {
            Util.getOperatingSystem().open(new URI(MOD_MENU_URL));
        } catch (URISyntaxException ex) {
            PhonkEditMod.LOGGER.warn("Failed to open Mod Menu page {}", MOD_MENU_URL, ex);
        }
        continueToParent();
    }
}
