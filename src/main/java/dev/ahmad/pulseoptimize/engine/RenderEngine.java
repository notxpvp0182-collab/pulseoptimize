package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.config.PulseConfig;

/**
 * Exposes rendering optimisation flags consulted by rendering mixins.
 * <p>
 * All optimisations here are client-side visual changes only.
 */
public class RenderEngine {

    private final PulseConfig config;
    @SuppressWarnings("unused")
    private final SafeMode safeMode;

    public RenderEngine(PulseConfig config, SafeMode safeMode) {
        this.config = config;
        this.safeMode = safeMode;
    }

    // ── Low Fire ──────────────────────────────────────────────────────────────

    public boolean isLowFireEnabled()    { return config.lowFire_enabled; }
    public float getLowFireHeight()      { return config.lowFire_height; }
    public float getLowFireScale()       { return config.lowFire_scale; }
    public float getLowFireOpacity()     { return config.lowFire_opacity; }

    // ── Water ─────────────────────────────────────────────────────────────────

    public boolean isClearWaterEnabled()               { return config.clearWater_enabled; }
    public boolean isWaterFogReduced()                 { return config.waterFog_reduced; }
    public boolean isUnderwaterParticlesReduced()      { return config.underwater_particles_reduced; }

    // ── Explosions ────────────────────────────────────────────────────────────

    public boolean isExplosionParticlesReduced()       { return config.reduceExplosion_particles; }
    public boolean isExplosionSmokeReduced()           { return config.reduceExplosion_smoke; }
    public boolean isExplosionDebrisReduced()          { return config.reduceExplosion_debris; }
    public boolean isExplosionScreenEffectsReduced()   { return config.reduceExplosion_screenEffects; }

    // ── Combat ────────────────────────────────────────────────────────────────

    public boolean isCrystalRenderingOptimized()       { return config.crystal_rendering_optimized; }
    public boolean isCrystalParticlesReduced()         { return config.crystal_particles_reduced; }
    public boolean isCrystalAnimationReduced()         { return config.crystal_animation_reduced; }
    public boolean isItemRenderingOptimized()          { return config.item_rendering_optimized; }

    // ── Environment ───────────────────────────────────────────────────────────

    public boolean isFogReduced()                      { return config.fog_reduced; }
    public boolean isRainOptimized()                   { return config.rain_optimized; }
    public boolean isSnowOptimized()                   { return config.snow_optimized; }
    public boolean isCloudOptimized()                  { return config.cloud_optimized; }
    public boolean isPortalEffectOptimized()           { return config.portalEffect_optimized; }
    public boolean isPotionEffectOptimized()           { return config.potionEffect_optimized; }
}
