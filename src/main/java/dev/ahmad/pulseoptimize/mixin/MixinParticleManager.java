package dev.ahmad.pulseoptimize.mixin;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.engine.ParticleEngine;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts {@code ParticleManager#addParticle} to apply PulseOptimize particle
 * level limits. Particles are cancelled at the mixin level by returning early;
 * game mechanics that depend on particles are never affected.
 */
@Mixin(ParticleManager.class)
public class MixinParticleManager {

    /**
     * Inject just before {@code addParticle} adds a particle to the live list.
     * Maps the particle type to a {@link ParticleEngine.ParticleCategory} and
     * asks {@link ParticleEngine#shouldSpawnParticle} whether to allow it.
     */
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
            at = @At("HEAD"),
            cancellable = true)
    private void pulseoptimize_onAddParticle(ParticleEffect parameters,
                                             double x, double y, double z,
                                             double velocityX, double velocityY, double velocityZ,
                                             CallbackInfo ci) {
        ParticleEngine engine = PulseOptimize.getParticleEngine();
        if (engine == null) return;

        ParticleEngine.ParticleCategory category = mapCategory(parameters);
        if (!engine.shouldSpawnParticle(category)) {
            ci.cancel();
        }
    }

    /**
     * Maps a {@link ParticleEffect} to a broad {@link ParticleEngine.ParticleCategory}.
     * Uses the particle type's identifier string for matching to avoid coupling to
     * specific particle type classes that may change between MC versions.
     */
    private static ParticleEngine.ParticleCategory mapCategory(ParticleEffect effect) {
        String id = effect.getType().toString().toLowerCase();

        if (id.contains("explosion") || id.contains("huge_explosion") || id.contains("large_smoke"))
            return ParticleEngine.ParticleCategory.EXPLOSION;
        if (id.contains("smoke") || id.contains("campfire"))
            return ParticleEngine.ParticleCategory.SMOKE;
        if (id.contains("flame") || id.contains("soul_fire"))
            return ParticleEngine.ParticleCategory.FIRE;
        if (id.contains("bubble") || id.contains("splash") || id.contains("fishing"))
            return ParticleEngine.ParticleCategory.WATER;
        if (id.contains("lava"))
            return ParticleEngine.ParticleCategory.LAVA;
        if (id.contains("effect") || id.contains("instant_effect") || id.contains("potion"))
            return ParticleEngine.ParticleCategory.POTION;
        if (id.contains("crit"))
            return ParticleEngine.ParticleCategory.CRIT_HIT;
        if (id.contains("enchant"))
            return ParticleEngine.ParticleCategory.ENCHANTMENT;
        if (id.contains("block") || id.contains("dust"))
            return ParticleEngine.ParticleCategory.BLOCK_BREAK;
        if (id.contains("falling"))
            return ParticleEngine.ParticleCategory.FALLING;
        if (id.contains("drip"))
            return ParticleEngine.ParticleCategory.DRIPPING;
        if (id.contains("portal") || id.contains("nether"))
            return ParticleEngine.ParticleCategory.PORTAL;
        if (id.contains("rain") || id.contains("snow") || id.contains("cloud"))
            return ParticleEngine.ParticleCategory.WEATHER;

        return ParticleEngine.ParticleCategory.AMBIENT;
    }
}
