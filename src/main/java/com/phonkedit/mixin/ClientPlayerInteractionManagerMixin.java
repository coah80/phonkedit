package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import com.phonkedit.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Unique private ItemStack phonkedit$preInteractStack = ItemStack.EMPTY;
    @Unique private int phonkedit$preInteractCount = 0;
    @Unique private BlockPos phonkedit$predictedPlacePos = null;
    
    @Inject(method = "breakBlock", at = @At("RETURN"))
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && ModConfig.INSTANCE.triggerOnBlockBreak) {
            PhonkEditClient.onBlockBroken();
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void phonkedit$capturePreInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        phonkedit$preInteractStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        phonkedit$preInteractCount = stack.isEmpty() ? 0 : stack.getCount();

        // Predict the position where a block would be placed (clicked face offset)
        BlockPos clicked = hitResult.getBlockPos();
        phonkedit$predictedPlacePos = clicked.offset(hitResult.getSide());
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onBlockPlaced(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (!ModConfig.INSTANCE.triggerOnBlockPlace) return;
        if (result == null || !result.isAccepted()) return;
        if (!(phonkedit$preInteractStack.getItem() instanceof BlockItem)) return;

        boolean placed = false;

        // Non-creative: detect by item count decrease
        if (!player.getAbilities().creativeMode) {
            ItemStack after = player.getStackInHand(hand);
            int afterCount = after.isEmpty() ? 0 : after.getCount();
            placed = afterCount < phonkedit$preInteractCount;
        }

        // Creative or fallback: check world state matches held block at predicted position
        if (!placed) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientWorld world = mc.world;
            if (world != null && phonkedit$predictedPlacePos != null) {
                BlockItem blockItem = (BlockItem) phonkedit$preInteractStack.getItem();
                placed = world.getBlockState(phonkedit$predictedPlacePos).getBlock() == blockItem.getBlock();
            }
        }

        if (placed) {
            PhonkEditClient.onBlockPlaced();
        }
    }
}
