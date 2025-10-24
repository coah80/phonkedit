package com.phonkedit;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static ItemGroup PHONKEDIT_GROUP;

    private ModItemGroups() {}

    public static void initialize() {
        PHONKEDIT_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(PhonkEditMod.MOD_ID, "phonkedit"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModBlocks.PHLONCK))
                        .displayName(Text.literal("Phonk Edit"))
                        .entries((context, entries) -> entries.add(ModBlocks.PHLONCK))
                        .build());
    }
}
