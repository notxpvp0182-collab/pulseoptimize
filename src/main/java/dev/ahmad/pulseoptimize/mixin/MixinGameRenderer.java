package dev.ahmad.pulseoptimize.mixin;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts {@code GameRenderer#render} to capture per-frame timing data for
 * {@link dev.ahmad.pulseoptimize.monitor.FpsMonitor} and
 * {@link dev.ahmad.pulseoptimize.monitor.FrameTimeMonitor}.
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    /** Nanosecond timestamp of the previous frame — used to compute delta. */
    private long pulseoptimize_lastFrameNanos = 0L;

    /**
     * Inject at the very start of the render method. The first call initialises
     * the timestamp; subsequent calls compute the delta and push it to monitors.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void pulseoptimize_onRenderHead(float tickProgress, long startTime, boolean tick,
                                            CallbackInfo ci) {
        long now = System.nanoTime();
        if (pulseoptimize_lastFrameNanos == 0L) {
            pulseoptimize_lastFrameNanos = now;
            return;
        }

        long delta = now - pulseoptimize_lastFrameNanos;
        pulseoptimize_lastFrameNanos = now;

        // Sanity guard: ignore deltas that are implausibly large (e.g. after a pause)
        if (delta <= 0 || delta > 5_000_000_000L) return;

        if (PulseOptimize.getFpsMonitor() != null) {
            PulseOptimize.getFpsMonitor().onFrame(delta);
        }
        if (PulseOptimize.getFrameTimeMonitor() != null) {
            PulseOptimize.getFrameTimeMonitor().onFrame(delta);
        }
    }
}
