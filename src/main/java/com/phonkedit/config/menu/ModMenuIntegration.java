package com.phonkedit.config.menu;

import com.phonkedit.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Phonk Edit Configuration"))
                    .setSavingRunnable(ModConfig::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            
            // General Settings Category
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General Settings"));
            
            general.addEntry(entryBuilder.startBooleanToggle(
                    Text.literal("Enable Phonk Effect"),
                    ModConfig.INSTANCE.enablePhonkEffect)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Enable or disable the phonk freeze effect"))
                    .setSaveConsumer(value -> ModConfig.INSTANCE.enablePhonkEffect = value)
                    .build());
            
            general.addEntry(entryBuilder.startDoubleField(
                    Text.literal("Trigger Chance"),
                    ModConfig.INSTANCE.triggerChance)
                    .setDefaultValue(0.10)
                    .setMin(0.0)
                    .setMax(1.0)
                    .setTooltip(Text.literal("Chance of effect triggering (0.0 = never, 1.0 = always)"))
                    .setSaveConsumer(value -> ModConfig.INSTANCE.triggerChance = value)
                    .build());
            
            general.addEntry(entryBuilder.startIntField(
                    Text.literal("Effect Duration (ms)"),
                    ModConfig.INSTANCE.effectDuration)
                    .setDefaultValue(3000)
                    .setMin(500)
                    .setMax(10000)
                    .setTooltip(Text.literal("How long the effect lasts in milliseconds"))
                    .setSaveConsumer(value -> ModConfig.INSTANCE.effectDuration = value)
                    .build());
            
            // Visual Effects Category
            ConfigCategory visual = builder.getOrCreateCategory(Text.literal("Visual Effects"));
            
            visual.addEntry(entryBuilder.startDoubleField(
                    Text.literal("Shake Intensity"),
                    ModConfig.INSTANCE.shakeIntensity)
                    .setDefaultValue(1.0)
                    .setMin(0.0)
                    .setMax(20.0)
                    .setTooltip(Text.literal("Skull shake intensity multiplier"),
                               Text.literal("1.0 = normal, 5.0 = crazy, 10.0 = outrageous!"))
                    .setSaveConsumer(value -> ModConfig.INSTANCE.shakeIntensity = value)
                    .build());

            return builder.build();
        };
    }
}
