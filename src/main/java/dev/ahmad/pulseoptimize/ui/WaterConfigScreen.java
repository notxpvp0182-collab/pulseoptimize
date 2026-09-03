package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** Water optimisation options. */
public class WaterConfigScreen extends BaseConfigScreen {

    public WaterConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.water");
    }

    @Override
    protected void buildContent() {
        addToggle("pulseoptimize.config.clear_water",
                () -> config.clearWater_enabled, v -> config.clearWater_enabled = v);
        addToggle("pulseoptimize.config.water_fog",
                () -> config.waterFog_reduced, v -> config.waterFog_reduced = v);
        addToggle("pulseoptimize.config.underwater_particles",
                () -> config.underwater_particles_reduced, v -> config.underwater_particles_reduced = v);
    }
}
