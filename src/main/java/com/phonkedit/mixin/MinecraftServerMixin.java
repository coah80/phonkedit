package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    
    @Inject(method = "tickWorlds", at = @At("HEAD"), cancellable = true)
    private void onTickWorlds(CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.pauseServerDuringEffect) {
            ci.cancel();
        }
    }
}
