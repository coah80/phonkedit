package com.phonkedit.mixin;

import com.phonkedit.state.ServerFreezeState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void phonkedit$protectDuringEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ServerFreezeState.isDamageProtected()) {
            cir.setReturnValue(false);
        }
    }
}
