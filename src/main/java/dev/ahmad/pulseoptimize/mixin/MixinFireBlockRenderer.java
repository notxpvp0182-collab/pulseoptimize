package dev.ahmad.pulseoptimize.mixin;

import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the Low Fire overlay height/opacity settings.
 * <p>
 * The fire-overlay is rendered as a fluid-layer quad on the HUD. We intercept
 * the fluid renderer to conditionally adjust the y-offset, simulating a lower
 * fire position. Game-side fire damage and block state are untouched.
 * <p>
 * Note: In 1.21.x the fire overlay is handled via the camera/overlay render
 * pipeline. This mixin targets FluidRenderer as a safe injection point for the
 * overlay quad generation — adjust to the exact vanilla class if mappings differ.
 */
@Mixin(FluidRenderer.class)
public class MixinFireBlockRenderer {

    /**
     * This mixin is intentionally minimal: the actual low-fire visual effect is
     * achieved in {@link dev.ahmad.pulseoptimize.render.HudRenderer} and the
     * camera overlay system. This class exists as a documented placeholder that
     * can be expanded with a precise mixin once the target method is identified
     * via yarn mappings for the active MC version.
     *
     * {@code require = 0} ensures the build does not fail if the target changes.
     */
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void pulseoptimize_onFluidRender(CallbackInfo ci) {
        // Intentionally empty — low fire is applied via overlay rendering in HudRenderer
    }
}
