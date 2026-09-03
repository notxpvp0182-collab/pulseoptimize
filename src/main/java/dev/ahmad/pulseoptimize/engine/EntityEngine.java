package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.MinecraftClient;

/**
 * Tracks entity counts and controls entity / block-entity culling behaviour.
 * <p>
 * Culling here means skipping the render call for entities that are provably
 * not visible to the camera — a safe client-side optimisation that does not
 * affect server-side simulation.
 */
public class EntityEngine {

    private final PulseConfig config;
    @SuppressWarnings("unused")
    private final SafeMode safeMode;

    private int trackedEntityCount = 0;

    public EntityEngine(PulseConfig config, SafeMode safeMode) {
        this.config = config;
        this.safeMode = safeMode;
    }

    /**
     * Refreshes the entity count from the current client world.
     * Called periodically by the performance engine — not every frame.
     */
    public void refresh(MinecraftClient client) {
        if (client.world != null) {
            // Iterate entities in loaded sections — count only
            trackedEntityCount = 0;
            for (var entity : client.world.getEntities()) {
                trackedEntityCount++;
            }
        }
    }

    /**
     * Returns {@code true} if entity culling is enabled and the mod should
     * attempt to skip rendering for off-screen entities.
     */
    public boolean isCullingEnabled() {
        return config.entityCulling_enabled;
    }

    public boolean isBlockEntityCullingEnabled() {
        return config.blockEntityCulling_enabled;
    }

    public int getTrackedEntityCount() { return trackedEntityCount; }
}
