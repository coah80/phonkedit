package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void phonkedit$freezeMovement(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && !com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void phonkedit$zeroHorizontalVelocity(CallbackInfo ci) {
        if (!PhonkEditClient.isFreezeModeActive() || com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        player.setVelocity(0.0, 0.0, 0.0);
    }
}
