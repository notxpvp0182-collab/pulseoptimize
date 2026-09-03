package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** Animation enable/disable configuration. */
public class AnimationConfigScreen extends BaseConfigScreen {

    public AnimationConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.animations");
    }

    @Override
    protected void buildContent() {
        addToggle("pulseoptimize.config.anim_water",       () -> config.water_animation,       v -> config.water_animation       = v);
        addToggle("pulseoptimize.config.anim_lava",        () -> config.lava_animation,        v -> config.lava_animation        = v);
        addToggle("pulseoptimize.config.anim_fire",        () -> config.fire_animation,        v -> config.fire_animation        = v);
        addToggle("pulseoptimize.config.anim_portal",      () -> config.portal_animation,      v -> config.portal_animation      = v);
        addToggle("pulseoptimize.config.anim_enchantment", () -> config.enchantment_animation, v -> config.enchantment_animation = v);
        addToggle("pulseoptimize.config.anim_terrain",     () -> config.terrain_animation,     v -> config.terrain_animation     = v);
        addToggle("pulseoptimize.config.anim_weather",     () -> config.weather_animation,     v -> config.weather_animation     = v);
    }
}
