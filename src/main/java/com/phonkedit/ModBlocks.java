package com.phonkedit;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block PHLONCK = register("phlonck", new Block(Block.Settings.copy(Blocks.DIAMOND_BLOCK)));

    private ModBlocks() {
    }

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(PhonkEditMod.MOD_ID, name);
        Block registered = Registry.register(Registries.BLOCK, id, block);
    Registry.register(Registries.ITEM, id, new BlockItem(registered, new Item.Settings()));
        return registered;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(PHLONCK));
    }
}
