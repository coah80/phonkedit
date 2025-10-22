package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    
    @Shadow
    private float pitch;
    
    @Shadow
    private float yaw;
    
    private static float frozenPitch;
    private static float frozenYaw;
    private static boolean capturedRotation = false;
    
    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateStart(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        boolean freezeCamera = PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect;
        if (freezeCamera) {
            if (!capturedRotation) {
                frozenPitch = this.pitch;
                frozenYaw = this.yaw;
                capturedRotation = true;
            }
        } else {
            capturedRotation = false;
        }
    }
    
    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateEnd(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (PhonkEditClient.isFreezeModeActive() && ModConfig.INSTANCE.lockCameraDuringEffect && capturedRotation) {
            this.pitch = frozenPitch;
            this.yaw = frozenYaw;
        }
    }
}
