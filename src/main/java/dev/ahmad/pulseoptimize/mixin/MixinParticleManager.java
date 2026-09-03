package dev.ahmad.pulseoptimize.mixin;
import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.engine.ParticleEngine;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulseoptimize_onAddParticleZ(ParticleEffect p, boolean forced, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci) {
        ParticleEngine e = PulseOptimize.getParticleEngine();
        if (e != null && !e.shouldSpawnParticle(map(p))) ci.cancel();
    }
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulseoptimize_onAddParticle(ParticleEffect p, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci) {
        ParticleEngine e = PulseOptimize.getParticleEngine();
        if (e != null && !e.shouldSpawnParticle(map(p))) ci.cancel();
    }
    private static ParticleEngine.ParticleCategory map(ParticleEffect effect) {
        String id = effect.getType().toString().toLowerCase();
        if (id.contains("explosion") || id.contains("large_smoke")) return ParticleEngine.ParticleCategory.EXPLOSION;
        if (id.contains("smoke") || id.contains("campfire")) return ParticleEngine.ParticleCategory.SMOKE;
        if (id.contains("flame") || id.contains("soul_fire")) return ParticleEngine.ParticleCategory.FIRE;
        if (id.contains("bubble") || id.contains("splash")) return ParticleEngine.ParticleCategory.WATER;
        if (id.contains("lava")) return ParticleEngine.ParticleCategory.LAVA;
        if (id.contains("effect") || id.contains("potion")) return ParticleEngine.ParticleCategory.POTION;
        if (id.contains("crit")) return ParticleEngine.ParticleCategory.CRIT_HIT;
        if (id.contains("enchant")) return ParticleEngine.ParticleCategory.ENCHANTMENT;
        if (id.contains("block") || id.contains("dust")) return ParticleEngine.ParticleCategory.BLOCK_BREAK;
        if (id.contains("falling")) return ParticleEngine.ParticleCategory.FALLING;
        if (id.contains("drip")) return ParticleEngine.ParticleCategory.DRIPPING;
        if (id.contains("portal") || id.contains("nether")) return ParticleEngine.ParticleCategory.PORTAL;
        if (id.contains("rain") || id.contains("snow")) return ParticleEngine.ParticleCategory.WEATHER;
        return ParticleEngine.ParticleCategory.AMBIENT;
    }
}
