package com.phonkedit.block.ritual;

import com.phonkedit.ModAdvancements;
import com.phonkedit.ModBlocks;
import com.phonkedit.network.NetworkHandler;
import com.phonkedit.state.PhonkCurseState;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;

public final class PhlonckRitual {
    private PhlonckRitual() {
    }

    public static boolean start(ServerWorld world, BlockPos pos, BlockState state, ItemEntity diamondEntity) {
        if (!state.isOf(ModBlocks.PHLONCK)) {
            return false;
        }
        if (!diamondEntity.getStack().isOf(Items.DIAMOND)) {
            return false;
        }
        MinecraftServer server = world.getServer();
        if (server == null) {
            return false;
        }
        if (PhonkCurseState.get(server).isCurseBroken()) {
            return false;
        }
        Vec3d center = Vec3d.ofCenter(pos).add(0.0, 0.1, 0.0);
        consumeDiamond(diamondEntity);
        world.removeBlock(pos, false);
        spawnParticles(world, center);
        playSounds(world, center);
        applyShockwave(world, center);
        notifyPlayers(world, server);
        return true;
    }

    private static void consumeDiamond(ItemEntity diamondEntity) {
        ItemStack stack = diamondEntity.getStack();
        stack.decrement(1);
        if (stack.isEmpty()) {
            diamondEntity.discard();
        } else {
            diamondEntity.setStack(stack);
        }
    }

    private static void spawnParticles(ServerWorld world, Vec3d center) {
        for (int layer = 0; layer < 4; layer++) {
            double radius = 0.4 + layer * 0.25;
            int count = 32 + layer * 12;
            double height = 0.2 + layer * 0.2;
            for (int i = 0; i < count; i++) {
                double angle = (2.0 * Math.PI * i / count) + world.random.nextDouble() * 0.2;
                double x = center.x + Math.cos(angle) * radius;
                double y = center.y + height;
                double z = center.z + Math.sin(angle) * radius;
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        world.spawnParticles(ParticleTypes.SMOKE, center.x, center.y + 0.3, center.z, 80, 0.6, 0.6, 0.6, 0.01);
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.4, center.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private static void playSounds(ServerWorld world, Vec3d center) {
        world.playSound(null, center.x, center.y, center.z, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 2.0f, 0.6f);
        world.playSound(null, center.x, center.y, center.z, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5f, 0.5f);
        world.playSound(null, center.x, center.y, center.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 2.0f, 0.8f);
    }

    private static void applyShockwave(ServerWorld world, Vec3d center) {
        List<ServerPlayerEntity> nearby = world.getPlayers(player -> player.squaredDistanceTo(center) <= 36.0);
        for (ServerPlayerEntity player : nearby) {
            player.damage(world.getDamageSources().magic(), 4.0f);
        }
    }

    private static void notifyPlayers(ServerWorld world, MinecraftServer server) {
        PhonkCurseState state = PhonkCurseState.get(server);
        state.setCurseBroken(true);
        NetworkHandler.broadcastCurseStatus(server, true);
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 20));
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("CURSE BROKEN").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("The Phonk Edit Curse Has Been Lifted!").formatted(Formatting.GRAY)));
            ModAdvancements.grantSkullMeOnce(player);
        }
    }
}
