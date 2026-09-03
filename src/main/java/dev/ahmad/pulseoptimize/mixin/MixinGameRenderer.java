package dev.ahmad.pulseoptimize.mixin;
import dev.ahmad.pulseoptimize.PulseOptimize;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    private long pulseoptimize_lastFrameNanos = 0L;
    @Inject(method = "render", at = @At("HEAD"))
    private void pulseoptimize_onRenderHead(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        long now = System.nanoTime();
        if (pulseoptimize_lastFrameNanos == 0L) { pulseoptimize_lastFrameNanos = now; return; }
        long delta = now - pulseoptimize_lastFrameNanos;
        pulseoptimize_lastFrameNanos = now;
        if (delta <= 0 || delta > 5_000_000_000L) return;
        if (PulseOptimize.getFpsMonitor() != null) PulseOptimize.getFpsMonitor().onFrame(delta);
        if (PulseOptimize.getFrameTimeMonitor() != null) PulseOptimize.getFrameTimeMonitor().onFrame(delta);
    }
}
