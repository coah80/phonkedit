package com.phonkedit.config.menu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Cloth Config is now a required dependency; always use the Cloth builder screen
        return parent -> ModConfigClothScreen.create(parent);
    }
}
