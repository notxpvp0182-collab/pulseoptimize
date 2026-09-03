package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** Environmental visual effect settings. */
public class EnvironmentConfigScreen extends BaseConfigScreen {

    public EnvironmentConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.environment");
    }

    @Override
    protected void buildContent() {
        addToggle("pulseoptimize.config.fog_reduce",
                () -> config.fog_reduced, v -> config.fog_reduced = v);
        addToggle("pulseoptimize.config.rain_opt",
                () -> config.rain_optimized, v -> config.rain_optimized = v);
        addToggle("pulseoptimize.config.snow_opt",
                () -> config.snow_optimized, v -> config.snow_optimized = v);
        addToggle("pulseoptimize.config.cloud_opt",
                () -> config.cloud_optimized, v -> config.cloud_optimized = v);
        addToggle("pulseoptimize.config.portal_effect",
                () -> config.portalEffect_optimized, v -> config.portalEffect_optimized = v);
        addToggle("pulseoptimize.config.potion_effect",
                () -> config.potionEffect_optimized, v -> config.potionEffect_optimized = v);
    }
}
