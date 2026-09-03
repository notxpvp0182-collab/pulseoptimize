package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.config.PulseConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls and monitors client-side particle spawning.
 * <p>
 * The mixin {@code MixinParticleManager} calls {@link #shouldSpawnParticle(ParticleCategory)}
 * before adding any particle — this engine returns whether the configured level
 * permits an additional particle of that category.
 */
public class ParticleEngine {

    private final PulseConfig config;
    @SuppressWarnings("unused")
    private final SafeMode safeMode;

    /** Live count of active particles this tick (approximate). */
    private final AtomicInteger activeParticleCount = new AtomicInteger(0);

    /** Max particles allowed per category at each quality level. */
    private static final int MAX_NORMAL   = Integer.MAX_VALUE;
    private static final int MAX_REDUCED  = 500;
    private static final int MAX_LOW      = 100;

    public ParticleEngine(PulseConfig config, SafeMode safeMode) {
        this.config = config;
        this.safeMode = safeMode;
    }

    /**
     * Called by the particle manager mixin before spawning a particle.
     *
     * @param category the category of the particle being spawned
     * @return {@code true} if the particle may be spawned, {@code false} to skip it
     */
    public boolean shouldSpawnParticle(ParticleCategory category) {
        PulseConfig.ParticleLevel level = getLevelForCategory(category);
        return switch (level) {
            case OFF     -> false;
            case LOW     -> activeParticleCount.get() < MAX_LOW;
            case REDUCED -> activeParticleCount.get() < MAX_REDUCED;
            case NORMAL  -> true;
        };
    }

    private PulseConfig.ParticleLevel getLevelForCategory(ParticleCategory cat) {
        return switch (cat) {
            case EXPLOSION    -> config.explosion_particles;
            case SMOKE        -> config.smoke_particles;
            case FIRE         -> config.fire_particles;
            case WATER        -> config.water_particles;
            case LAVA         -> config.lava_particles;
            case POTION       -> config.potion_particles;
            case CRIT_HIT     -> config.critHit_particles;
            case ENCHANTMENT  -> config.enchantment_particles;
            case BLOCK_BREAK  -> config.blockBreak_particles;
            case FALLING      -> config.falling_particles;
            case DRIPPING     -> config.dripping_particles;
            case PORTAL       -> config.portal_particles;
            case AMBIENT      -> config.ambient_particles;
            case WEATHER      -> config.weather_particles;
        };
    }

    /** Called each tick by the particle manager to update the live count. */
    public void setActiveParticleCount(int count) {
        activeParticleCount.set(count);
    }

    public int getActiveParticleCount() { return activeParticleCount.get(); }

    public enum ParticleCategory {
        EXPLOSION, SMOKE, FIRE, WATER, LAVA, POTION, CRIT_HIT,
        ENCHANTMENT, BLOCK_BREAK, FALLING, DRIPPING, PORTAL, AMBIENT, WEATHER
    }
}
