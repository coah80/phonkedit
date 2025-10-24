package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void phonkedit$cancelWorldTick(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) ci.cancel();
    }

    @Inject(method = "tickEntities", at = @At("HEAD"), cancellable = true)
    private void phonkedit$cancelEntityTick(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) ci.cancel();
    }
}
