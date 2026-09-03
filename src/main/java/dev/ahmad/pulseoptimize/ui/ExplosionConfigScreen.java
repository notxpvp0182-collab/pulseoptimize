package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** Explosion visual reduction settings. */
public class ExplosionConfigScreen extends BaseConfigScreen {

    public ExplosionConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.explosions");
    }

    @Override
    protected void buildContent() {
        addToggle("pulseoptimize.config.exp_particles",
                () -> config.reduceExplosion_particles, v -> config.reduceExplosion_particles = v);
        addToggle("pulseoptimize.config.exp_smoke",
                () -> config.reduceExplosion_smoke, v -> config.reduceExplosion_smoke = v);
        addToggle("pulseoptimize.config.exp_debris",
                () -> config.reduceExplosion_debris, v -> config.reduceExplosion_debris = v);
        addToggle("pulseoptimize.config.exp_screen",
                () -> config.reduceExplosion_screenEffects, v -> config.reduceExplosion_screenEffects = v);
    }
}
