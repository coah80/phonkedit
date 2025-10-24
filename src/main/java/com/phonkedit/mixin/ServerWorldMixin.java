package com.phonkedit.mixin;

import com.phonkedit.state.ServerFreezeState;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void phonkedit$pauseWorld(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (ServerFreezeState.isWorldFrozen()) {
            ci.cancel();
        }
    }
}
