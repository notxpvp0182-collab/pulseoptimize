package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/**
 * Combat / PvP performance settings.
 * <p>
 * All options here are purely visual/client-side. No gameplay mechanics,
 * hitboxes, reach, or targeting are modified.
 */
public class CombatConfigScreen extends BaseConfigScreen {

    public CombatConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.combat");
    }

    @Override
    protected void buildContent() {
        addLabel("Visual optimisations only — no gameplay changes");
        addToggle("pulseoptimize.config.crystal_render",
                () -> config.crystal_rendering_optimized, v -> config.crystal_rendering_optimized = v);
        addToggle("pulseoptimize.config.crystal_particles",
                () -> config.crystal_particles_reduced, v -> config.crystal_particles_reduced = v);
        addToggle("pulseoptimize.config.crystal_anim",
                () -> config.crystal_animation_reduced, v -> config.crystal_animation_reduced = v);
        addToggle("pulseoptimize.config.item_render",
                () -> config.item_rendering_optimized, v -> config.item_rendering_optimized = v);
    }
}
