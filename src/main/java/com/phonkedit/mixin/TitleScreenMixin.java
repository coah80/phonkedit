package com.phonkedit.mixin;

import com.phonkedit.config.ModConfig;
import com.phonkedit.config.menu.DisclaimerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (com.phonkedit.client.PhonkEditClient.skipNextDisclaimerCheck) {
            com.phonkedit.client.PhonkEditClient.skipNextDisclaimerCheck = false;
            return;
        }
        boolean missingModMenu = !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmenu");
        if (missingModMenu && !ModConfig.INSTANCE.modMenuDisclaimerShown) {
            MinecraftClient.getInstance().setScreen(new DisclaimerScreen(new TitleScreen()));
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (com.phonkedit.client.PhonkEditClient.skipNextDisclaimerCheck) {
            return;
        }
        boolean missingModMenu = !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmenu");
        if (missingModMenu && !ModConfig.INSTANCE.modMenuDisclaimerShown) {
            MinecraftClient.getInstance().setScreen(new DisclaimerScreen(new TitleScreen()));
            ci.cancel();
        }
    }
}
