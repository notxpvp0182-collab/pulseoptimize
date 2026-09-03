package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitors client-side chunk load events and maintains a lightweight count of
 * pending/loading chunks. The data is consumed by {@link PerformanceEngine} for
 * workload estimation.
 * <p>
 * This engine does <em>not</em> modify chunk generation, world data, or server
 * networking. It only observes client-visible chunk events.
 */
public class ChunkEngine {

    private final PulseConfig config;
    private final SafeMode safeMode;

    /** Approximate count of chunks currently being loaded client-side. */
    private final AtomicInteger pendingCount = new AtomicInteger(0);
    private int loadedCount = 0;

    public ChunkEngine(PulseConfig config, SafeMode safeMode) {
        this.config = config;
        this.safeMode = safeMode;

        // Register chunk load/unload events (Fabric API)
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            // Chunk arrived — decrement pending estimate
            int pending = pendingCount.decrementAndGet();
            if (pending < 0) pendingCount.set(0);
        });

        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            // Not tracked for pending count
        });
    }

    /**
     * Should be called when the client requests a new chunk from the server.
     * Invoked by the world renderer mixin on chunk update scheduling.
     */
    public void onChunkRequested() {
        pendingCount.incrementAndGet();
    }

    /**
     * Refreshes the loaded chunk count from the current client world.
     * Called periodically — not every frame.
     */
    public void refresh(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world != null) {
            loadedCount = world.getChunkManager().getLoadedChunkCount();
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int getPendingChunkCount() { return Math.max(0, pendingCount.get()); }
    public int getLoadedChunkCount() { return loadedCount; }

    /** Returns the schedule label from config for HUD display. */
    public String getScheduleLabel() {
        return switch (config.chunkSchedule) {
            case CONSERVATIVE -> "CONSERVATIVE";
            case BALANCED -> "BALANCED";
            case AGGRESSIVE -> "AGGRESSIVE";
        };
    }
}
