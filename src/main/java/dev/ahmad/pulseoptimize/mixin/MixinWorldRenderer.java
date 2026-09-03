package dev.ahmad.pulseoptimize.mixin;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts {@code WorldRenderer} to notify the {@link dev.ahmad.pulseoptimize.engine.ChunkEngine}
 * when chunk build tasks are submitted. This enables a passive pending-chunk counter
 * without interfering with the actual chunk scheduling logic.
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    /**
     * Intercept chunk rebuild scheduling to increment the pending counter.
     * The injection target is the method that adds a chunk to the build queue;
     * the exact method name uses Yarn 1.21.1 mappings.
     */
    @Inject(
        method = "scheduleChunkRender(IIIZ)V",
        at = @At("HEAD"),
        require = 0  // Non-fatal if the method signature changes between MC versions
    )
    private void pulseoptimize_onChunkRenderScheduled(int x, int y, int z, boolean z2, CallbackInfo ci) {
        if (PulseOptimize.getChunkEngine() != null) {
            PulseOptimize.getChunkEngine().onChunkRequested();
        }
    }
}
