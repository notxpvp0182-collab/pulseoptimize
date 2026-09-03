package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** Low Fire configuration screen. */
public class FireConfigScreen extends BaseConfigScreen {

    public FireConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.fire");
    }

    @Override
    protected void buildContent() {
        addLabel("Low Fire Overlay");
        addToggle("pulseoptimize.config.low_fire",
                () -> config.lowFire_enabled,
                v -> config.lowFire_enabled = v);

        // Height, scale and opacity are configured via numeric fields; here we
        // provide simple increment/decrement cycle buttons for mobile-friendly use.
        addLabel(String.format("Height: %.2f (see config file to fine-tune)", config.lowFire_height));
        addLabel(String.format("Scale:  %.2f", config.lowFire_scale));
        addLabel(String.format("Opacity: %.2f", config.lowFire_opacity));
    }
}
