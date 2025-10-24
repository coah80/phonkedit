package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(boolean tick, CallbackInfo ci) {
        if (!PhonkEditClient.isFreezeModeActive()) {
            return;
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void phonkedit$cancelInput(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) ci.cancel();
    }
}
