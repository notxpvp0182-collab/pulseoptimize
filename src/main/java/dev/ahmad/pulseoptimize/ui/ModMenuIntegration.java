package dev.ahmad.pulseoptimize.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ahmad.pulseoptimize.PulseOptimize;

/**
 * Registers PulseOptimize with Mod Menu, providing a factory that opens the
 * main configuration screen when the user clicks the "Config" button.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new PulseConfigScreen(parent, PulseOptimize.getConfig());
    }
}
