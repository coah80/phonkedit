package com.phonkedit;

import com.phonkedit.block.PhlonckBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block PHLONCK = register("phlonck", new PhlonckBlock(Block.Settings.copy(Blocks.DIAMOND_BLOCK).ticksRandomly().sounds(new BlockSoundGroup(
        1.0f,
        1.0f,
        SoundEvent.of(Identifier.ofVanilla("block.diamond_block.break")),
        SoundEvent.of(Identifier.ofVanilla("block.diamond_block.step")),
        SoundEvent.of(Identifier.ofVanilla("block.soul_sand.place")),
        SoundEvent.of(Identifier.ofVanilla("block.diamond_block.hit")),
        SoundEvent.of(Identifier.ofVanilla("block.diamond_block.fall"))
    ))));

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
