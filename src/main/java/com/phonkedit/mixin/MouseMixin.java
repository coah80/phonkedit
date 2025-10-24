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
    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void phonkedit$lockCamera(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect) {
            ci.cancel();
        }
    }
}
