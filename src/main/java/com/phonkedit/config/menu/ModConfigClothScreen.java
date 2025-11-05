package com.phonkedit.config.menu;

import com.phonkedit.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Cloth Config-backed configuration screen for Phonk Edit.
 */
public final class ModConfigClothScreen {
    private ModConfigClothScreen() {}

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("Phonk Edit Configuration"));
        builder.setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Phonk Effect"), config.enablePhonkEffect)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.enablePhonkEffect = value)
            .build());
        general.addEntry(entryBuilder.startDoubleField(Text.literal("Trigger Chance"), config.triggerChance)
            .setDefaultValue(0.30)
            .setMin(0.0)
            .setMax(1.0)
            .setSaveConsumer(value -> config.triggerChance = value)
            .build());
        general.addEntry(entryBuilder.startIntField(Text.literal("Effect Duration (ms)"), config.effectDuration)
            .setDefaultValue(3000)
            .setMin(250)
            .setMax(20000)
            .setSaveConsumer(value -> config.effectDuration = value)
            .build());
        general.addEntry(entryBuilder.startDoubleField(Text.literal("Shake Intensity"), config.shakeIntensity)
            .setDefaultValue(1.0)
            .setMin(0.0)
            .setMax(5.0)
            .setSaveConsumer(value -> config.shakeIntensity = value)
            .build());

        ConfigCategory audio = builder.getOrCreateCategory(Text.literal("Audio"));
        audio.addEntry(entryBuilder.startDoubleField(Text.literal("Pitch Min"), config.phonkPitchMin)
            .setDefaultValue(0.95)
            .setMin(0.5)
            .setMax(2.0)
            .setSaveConsumer(value -> config.phonkPitchMin = value)
            .build());
        audio.addEntry(entryBuilder.startDoubleField(Text.literal("Pitch Max"), config.phonkPitchMax)
            .setDefaultValue(1.05)
            .setMin(0.5)
            .setMax(2.0)
            .setSaveConsumer(value -> config.phonkPitchMax = value)
            .build());
        audio.addEntry(entryBuilder.startBooleanToggle(Text.literal("Only Use Custom Songs"), config.onlyUseCustomSongs)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.onlyUseCustomSongs = value)
            .build());

        ConfigCategory visuals = builder.getOrCreateCategory(Text.literal("Visuals"));
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Grayscale Freeze Frame"), config.grayscaleFreezeFrame)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.grayscaleFreezeFrame = value)
            .build());
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Darken Screen"), config.darkenScreenDuringEffect)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.darkenScreenDuringEffect = value)
            .build());
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Cinematic Bars"), config.showCinematicBars)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.showCinematicBars = value)
            .build());
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Render Skull Overlay"), config.renderSkullOverlay)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.renderSkullOverlay = value)
            .build());
        visuals.addEntry(entryBuilder.startDoubleField(Text.literal("Skull Scale"), config.skullScale)
            .setDefaultValue(0.4)
            .setMin(0.1)
            .setMax(2.0)
            .setSaveConsumer(value -> config.skullScale = value)
            .build());
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Skull Shake"), config.skullShakeEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.skullShakeEnabled = value)
            .build());
        visuals.addEntry(entryBuilder.startBooleanToggle(Text.literal("Skull Motion Blur"), config.skullBlurEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.skullBlurEnabled = value)
            .build());
        visuals.addEntry(entryBuilder.startDoubleField(Text.literal("Skull Blur Intensity"), config.skullBlurIntensity)
            .setDefaultValue(5.0)
            .setMin(0.0)
            .setMax(5.0)
            .setSaveConsumer(value -> config.skullBlurIntensity = value)
            .build());
        visuals.addEntry(entryBuilder.startDoubleField(Text.literal("Skull Blur Ease"), config.skullBlurEasePower)
            .setDefaultValue(1.5)
            .setMin(0.1)
            .setMax(5.0)
            .setSaveConsumer(value -> config.skullBlurEasePower = value)
            .build());

        ConfigCategory triggers = builder.getOrCreateCategory(Text.literal("Triggers"));
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Block Break"), config.triggerOnBlockBreak)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnBlockBreak = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Block Place"), config.triggerOnBlockPlace)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnBlockPlace = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Entity Hit"), config.triggerOnEntityHit)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnEntityHit = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Damage Taken"), config.triggerOnDamageTaken)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnDamageTaken = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Low Health"), config.triggerOnLowHealth)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnLowHealth = value)
            .build());
        triggers.addEntry(entryBuilder.startFloatField(Text.literal("Low Health Threshold"), config.lowHealthThreshold)
            .setDefaultValue(6.0f)
            .setMin(1.0f)
            .setMax(20.0f)
            .setSaveConsumer(value -> config.lowHealthThreshold = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Air Time"), config.triggerOnAirTime)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnAirTime = value)
            .build());
        triggers.addEntry(entryBuilder.startDoubleField(Text.literal("Air Time Threshold (s)"), config.airTimeThresholdSeconds)
            .setDefaultValue(1.3)
            .setMin(0.1)
            .setMax(10.0)
            .setSaveConsumer(value -> config.airTimeThresholdSeconds = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Lever Use"), config.triggerOnLeverUse)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnLeverUse = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Door Use"), config.triggerOnDoorUse)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnDoorUse = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Vehicle Mount"), config.triggerOnVehicleMount)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnVehicleMount = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Eating Food"), config.triggerOnEatFood)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnEatFood = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Bed Use"), config.triggerOnUseBed)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnUseBed = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Dragon Kill"), config.triggerOnDragonKill)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnDragonKill = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Wither Kill"), config.triggerOnWitherKill)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnWitherKill = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Warden Kill"), config.triggerOnWardenKill)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnWardenKill = value)
            .build());
        triggers.addEntry(entryBuilder.startBooleanToggle(Text.literal("Trigger on Elder Guardian Kill"), config.triggerOnElderGuardianKill)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.triggerOnElderGuardianKill = value)
            .build());

        ConfigCategory control = builder.getOrCreateCategory(Text.literal("Control"));
        control.addEntry(entryBuilder.startBooleanToggle(Text.literal("Pause Server During Effect"), config.pauseServerDuringEffect)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.pauseServerDuringEffect = value)
            .build());
        control.addEntry(entryBuilder.startBooleanToggle(Text.literal("Lock Mouse"), config.lockMouseDuringEffect)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.lockMouseDuringEffect = value)
            .build());
        control.addEntry(entryBuilder.startBooleanToggle(Text.literal("Lock Camera"), config.lockCameraDuringEffect)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.lockCameraDuringEffect = value)
            .build());
        control.addEntry(entryBuilder.startBooleanToggle(Text.literal("Hardcore Mode"), config.hardcoreMode)
            .setDefaultValue(false)
            .setTooltip(Text.literal("Disables pausing and camera/input locks during the effect."))
            .setSaveConsumer(value -> config.hardcoreMode = value)
            .build());

        ConfigCategory developer = builder.getOrCreateCategory(Text.literal("Developer"));
        developer.addEntry(entryBuilder.startBooleanToggle(Text.literal("Disable Pause On Lost Focus"), config.devDisablePauseOnLostFocus)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.devDisablePauseOnLostFocus = value)
            .build());
        developer.addEntry(entryBuilder.startBooleanToggle(Text.literal("Do Not End On Pause"), config.devDontEndOnPause)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.devDontEndOnPause = value)
            .build());

        return builder.build();
    }
}
