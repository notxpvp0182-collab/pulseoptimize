package dev.ahmad.pulseoptimize.mixin;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies fog reduction when {@code config.fog_reduced} is enabled.
 * <p>
 * We modify the fog colour alpha to reduce render-distance fog opacity.
 * This is a purely visual change; chunk loading distance and game mechanics
 * are not affected.
 */
@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    /**
     * Intercepts the fog colour setup to apply fog reduction.
     * {@code require = 0} prevents build failures on minor MC version differences.
     */
    @Inject(
        method = "applyFog",
        at = @At("TAIL"),
        require = 0
    )
    private static void pulseoptimize_onApplyFog(Camera camera,
                                                  BackgroundRenderer.FogType fogType,
                                                  Vector4f color,
                                                  float viewDistance,
                                                  boolean thickFog,
                                                  float tickProgress,
                                                  CallbackInfo ci) {
        if (PulseOptimize.getRenderEngine() == null) return;
        if (PulseOptimize.getRenderEngine().isFogReduced()) {
            // Reducing the alpha component of the fog colour makes distant chunks
            // appear less obscured without changing clip distances
            color.w = Math.max(0.0f, color.w * 0.5f);
        }
    }
}
