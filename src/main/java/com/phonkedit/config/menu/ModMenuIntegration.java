package com.phonkedit.config.menu;

import net.fabricmc.loader.api.FabricLoader;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {
        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
                        return parent -> {
                                if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                                        try {
                                                Class<?> screenClass = Class.forName("com.phonkedit.config.menu.ModConfigClothScreen");
                                                return (Screen) screenClass.getMethod("create", Screen.class).invoke(null, parent);
                                        } catch (Throwable ignored) {
                                        }
                                }
                                return new BasicConfigScreen(parent);
                        };
        }
}
