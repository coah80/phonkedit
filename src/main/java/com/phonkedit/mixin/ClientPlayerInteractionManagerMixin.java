package com.phonkedit.mixin;

import com.phonkedit.client.PhonkEditClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Unique
    private ItemStack phonkedit$capturedStack = ItemStack.EMPTY;
    @Unique
    private BlockPos phonkedit$capturedPlacementPos;
    @Unique
    private BlockState phonkedit$capturedPlacementState;

    @Inject(method = "breakBlock", at = @At("RETURN"))
    private void phonkedit$afterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            PhonkEditClient.onBlockBroken();
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void phonkedit$captureInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        phonkedit$capturedStack = player.getStackInHand(hand).copy();
        if (!(player.getWorld() instanceof ClientWorld clientWorld)) {
            phonkedit$capturedPlacementPos = null;
            phonkedit$capturedPlacementState = null;
            return;
        }
        BlockPos placementPos = hitResult.getBlockPos().offset(hitResult.getSide());
        phonkedit$capturedPlacementPos = placementPos;
        phonkedit$capturedPlacementState = clientWorld.getBlockState(placementPos);
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void phonkedit$afterInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (!(player.getWorld() instanceof ClientWorld clientWorld)) {
            phonkedit$clearCaptured();
            return;
        }
        if (result.isAccepted()) {
            if (!phonkedit$capturedStack.isEmpty() && phonkedit$capturedStack.getItem() instanceof BlockItem && phonkedit$capturedPlacementPos != null) {
                BlockState before = phonkedit$capturedPlacementState;
                BlockState after = clientWorld.getBlockState(phonkedit$capturedPlacementPos);
                if (!after.isAir() && (before == null || !after.equals(before))) {
                    PhonkEditClient.onBlockPlaced();
                }
            }
            if (!phonkedit$capturedStack.isEmpty()) {
                if (phonkedit$capturedStack.getItem() instanceof BoatItem || phonkedit$capturedStack.getItem() instanceof MinecartItem) {
                    PhonkEditClient.onVehicleMounted();
                }
            }
            BlockState targetState = clientWorld.getBlockState(hitResult.getBlockPos());
            Block block = targetState.getBlock();
            if (block instanceof LeverBlock || block instanceof ButtonBlock) {
                PhonkEditClient.onLeverUsed();
            } else if (block instanceof DoorBlock || block instanceof TrapdoorBlock || block instanceof FenceGateBlock) {
                PhonkEditClient.onDoorUsed();
            }
        }
        phonkedit$clearCaptured();
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void phonkedit$afterInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (!result.isAccepted()) return;
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isEmpty() && (stack.getItem() instanceof BoatItem || stack.getItem() instanceof MinecartItem)) {
            PhonkEditClient.onVehicleMounted();
        }
    }

    @Unique
    private void phonkedit$clearCaptured() {
        phonkedit$capturedStack = ItemStack.EMPTY;
        phonkedit$capturedPlacementPos = null;
        phonkedit$capturedPlacementState = null;
    }
}
