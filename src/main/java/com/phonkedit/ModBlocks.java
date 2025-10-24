package com.phonkedit;

import com.phonkedit.block.PhlonckBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block PHLONCK = register("phlonck", new PhlonckBlock(Block.Settings.copy(Blocks.BONE_BLOCK).ticksRandomly()));

    private ModBlocks() {
    }

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(PhonkEditMod.MOD_ID, name);
        Block registered = Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(registered, new Item.Settings()));
        return registered;
    }

    public static void initialize() {
        PhonkEditMod.LOGGER.info("Registered phlonck block");
    }
}
