package dev.ahmad.pulseoptimize.mixin;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders the PulseOptimize performance HUD overlay after the vanilla HUD has
 * drawn its own elements, so the HUD does not interfere with vanilla rendering.
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("TAIL"))
    private void pulseoptimize_onHudRender(DrawContext context, float tickProgress, CallbackInfo ci) {
        if (PulseOptimize.getHudRenderer() != null && PulseOptimize.getConfig() != null
                && PulseOptimize.getConfig().showHud) {
            PulseOptimize.getHudRenderer().render(context);
        }
    }
}
