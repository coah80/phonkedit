package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    
    @Inject(method = "breakBlock", at = @At("RETURN"))
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && ModConfig.INSTANCE.triggerOnBlockBreak) {
            PhonkEditClient.onBlockBroken();
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onBlockPlaced(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (result != null && result.isAccepted()) {
            if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
                PhonkEditClient.onBlockPlaced();
            }
        }
    }
}
