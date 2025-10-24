package com.phonkedit.block;

import com.phonkedit.block.ritual.PhlonckRitual;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PhlonckBlock extends Block {
    private static final int CHECK_INTERVAL_TICKS = 4;

    public PhlonckBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        scheduleCheck(world, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        scheduleCheck(world, pos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ItemEntity diamond = world.getEntitiesByClass(ItemEntity.class, detectionBox(pos), entity -> entity.getStack().isOf(Items.DIAMOND) && entity.isAlive()).stream().findFirst().orElse(null);
        if (diamond != null && PhlonckRitual.start(world, pos, state, diamond)) {
            return;
        }
        world.scheduleBlockTick(pos, this, CHECK_INTERVAL_TICKS);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        scheduleCheck(world, pos);
    }

    private void scheduleCheck(World world, BlockPos pos) {
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, CHECK_INTERVAL_TICKS);
        }
    }

    private Box detectionBox(BlockPos pos) {
        Vec3d center = Vec3d.ofCenter(pos).add(0.0, 0.4, 0.0);
        return Box.of(center, 1.6, 1.6, 1.6);
    }
}
