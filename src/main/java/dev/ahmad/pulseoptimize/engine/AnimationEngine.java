package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.config.PulseConfig;

/**
 * Provides animation enable/disable queries consumed by rendering mixins.
 * <p>
 * Disabling an animation must not break gameplay logic. Only purely cosmetic
 * animated textures are candidates for disabling.
 */
public class AnimationEngine {

    private final PulseConfig config;
    @SuppressWarnings("unused")
    private final SafeMode safeMode;

    public AnimationEngine(PulseConfig config, SafeMode safeMode) {
        this.config = config;
        this.safeMode = safeMode;
    }

    public boolean isWaterAnimationEnabled()       { return config.water_animation; }
    public boolean isLavaAnimationEnabled()        { return config.lava_animation; }
    public boolean isFireAnimationEnabled()        { return config.fire_animation; }
    public boolean isPortalAnimationEnabled()      { return config.portal_animation; }
    public boolean isEnchantmentAnimationEnabled() { return config.enchantment_animation; }
    public boolean isTerrainAnimationEnabled()     { return config.terrain_animation; }
    public boolean isWeatherAnimationEnabled()     { return config.weather_animation; }
}
