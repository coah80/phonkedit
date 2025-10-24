package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Input.class)
public class PlayerInputMixin {

    @Shadow public float movementForward;
    @Shadow public float movementSideways;
    @Shadow public boolean pressingForward;
    @Shadow public boolean pressingBack;
    @Shadow public boolean pressingLeft;
    @Shadow public boolean pressingRight;
    @Shadow public boolean jumping;
    @Shadow public boolean sneaking;

    @Inject(method = "tick", at = @At("TAIL"))
    private void phonkedit$haltInput(CallbackInfo ci) {
        if (!PhonkEditClient.isFreezeModeActive() || com.phonkedit.config.ModConfig.INSTANCE.hardcoreMode) {
            return;
        }

        this.movementForward = 0.0F;
        this.movementSideways = 0.0F;
        this.pressingForward = false;
        this.pressingBack = false;
        this.pressingLeft = false;
        this.pressingRight = false;
        this.jumping = false;
        this.sneaking = false;
    }
}
