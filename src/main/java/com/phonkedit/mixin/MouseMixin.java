package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @org.spongepowered.asm.mixin.Shadow private double cursorDeltaX;
    @org.spongepowered.asm.mixin.Shadow private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void phonkedit$lockCamera(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            this.cursorDeltaX = 0;
            this.cursorDeltaY = 0;
            ci.cancel();
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void phonkedit$eatCursorMove(long window, double x, double y, CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void phonkedit$noScroll(long window, double horiz, double vert, CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            ci.cancel();
        }
    }
}
