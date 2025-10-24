package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void phonkedit$cancelParticleTick(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive()) ci.cancel();
    }
}
