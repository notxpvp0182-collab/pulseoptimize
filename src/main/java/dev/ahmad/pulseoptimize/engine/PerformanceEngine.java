package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.compat.CompatibilityManager;
import dev.ahmad.pulseoptimize.config.PulseConfig;
import dev.ahmad.pulseoptimize.monitor.FpsMonitor;
import dev.ahmad.pulseoptimize.monitor.FrameTimeMonitor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Central performance orchestrator.
 * <p>
 * Polls subsystems on a tick-based schedule (not every rendered frame) to keep
 * overhead minimal. Detects FPS drops, diagnoses probable causes, fires the
 * warning system and — when auto-optimize is enabled — applies conservative
 * corrective measures with hysteresis.
 */
public class PerformanceEngine {

    // Tick cadence: run diagnostics every N client ticks (~50 ms per tick at 20 TPS)
    private static final int DIAGNOSTIC_INTERVAL_TICKS = 20; // every second
    private static final int AUTO_OPTIMIZE_COOLDOWN_TICKS = 200; // 10 s cooldown

    private final PulseConfig config;
    private final FpsMonitor fpsMonitor;
    private final FrameTimeMonitor frameTimeMonitor;
    private final MemoryEngine memoryEngine;
    private final ChunkEngine chunkEngine;
    private final RenderEngine renderEngine;
    private final EntityEngine entityEngine;
    private final ParticleEngine particleEngine;
    private final AnimationEngine animationEngine;
    private final CompatibilityManager compatManager;
    private final SafeMode safeMode;

    private int tickCounter = 0;
    private int autoOptimizeCooldown = 0;

    // Most recent drop event for the HUD/UI
    private DropEvent lastDropEvent = null;
    private final List<DropEvent> dropHistory = new ArrayList<>();
    private static final int MAX_DROP_HISTORY = 20;

    // Workload estimates — updated each diagnostic cycle
    private WorkloadLevel chunkWorkload = WorkloadLevel.UNKNOWN;
    private WorkloadLevel entityWorkload = WorkloadLevel.UNKNOWN;
    private WorkloadLevel particleWorkload = WorkloadLevel.UNKNOWN;
    private WorkloadLevel memoryWorkload = WorkloadLevel.UNKNOWN;

    public PerformanceEngine(
            PulseConfig config, FpsMonitor fpsMonitor, FrameTimeMonitor frameTimeMonitor,
            MemoryEngine memoryEngine, ChunkEngine chunkEngine, RenderEngine renderEngine,
            EntityEngine entityEngine, ParticleEngine particleEngine, AnimationEngine animationEngine,
            CompatibilityManager compatManager, SafeMode safeMode) {
        this.config = config;
        this.fpsMonitor = fpsMonitor;
        this.frameTimeMonitor = frameTimeMonitor;
        this.memoryEngine = memoryEngine;
        this.chunkEngine = chunkEngine;
        this.renderEngine = renderEngine;
        this.entityEngine = entityEngine;
        this.particleEngine = particleEngine;
        this.animationEngine = animationEngine;
        this.compatManager = compatManager;
        this.safeMode = safeMode;
    }

    /**
     * Registers the client-tick listener. Called once during mod init.
     */
    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    // ── Tick handler ─────────────────────────────────────────────────────────

    private void onClientTick(MinecraftClient client) {
        if (client.world == null) return;

        tickCounter++;
        if (autoOptimizeCooldown > 0) autoOptimizeCooldown--;

        if (tickCounter % DIAGNOSTIC_INTERVAL_TICKS == 0) {
            runDiagnosticCycle(client);
        }
    }

    private void runDiagnosticCycle(MinecraftClient client) {
        // Update workload estimates from subsystems
        updateWorkloadEstimates(client);

        int prevFps = fpsMonitor.getPreviousFps();
        int currFps = fpsMonitor.getCurrentFps();

        if (prevFps <= 0 || currFps <= 0) return;

        double dropRatio = 1.0 - ((double) currFps / prevFps);

        if (dropRatio >= config.fpsDrop_threshold && prevFps >= 20) {
            // Significant FPS drop detected
            DropEvent event = diagnose(prevFps, currFps);
            lastDropEvent = event;

            if (dropHistory.size() >= MAX_DROP_HISTORY) {
                dropHistory.remove(0);
            }
            dropHistory.add(event);

            PulseOptimize.LOGGER.info("[PulseOptimize] FPS drop detected: {} -> {} | Cause: {}",
                    prevFps, currFps, event.primaryCause);

            // Auto-optimize if enabled and cooldown elapsed
            if (config.autoOptimize && autoOptimizeCooldown <= 0 && !safeMode.isActive()) {
                applyAutoOptimize(event);
                autoOptimizeCooldown = AUTO_OPTIMIZE_COOLDOWN_TICKS;
            }
        }
    }

    // ── Workload estimation ───────────────────────────────────────────────────

    private void updateWorkloadEstimates(MinecraftClient client) {
        // Chunk workload: compare pending chunk builds
        int pending = chunkEngine.getPendingChunkCount();
        chunkWorkload = pending > 30 ? WorkloadLevel.HIGH
                : pending > 10 ? WorkloadLevel.MEDIUM
                : WorkloadLevel.LOW;

        // Entity workload: entity count heuristic
        int entities = entityEngine.getTrackedEntityCount();
        entityWorkload = entities > 300 ? WorkloadLevel.HIGH
                : entities > 100 ? WorkloadLevel.MEDIUM
                : WorkloadLevel.LOW;

        // Particle workload: particle count heuristic
        int particles = particleEngine.getActiveParticleCount();
        particleWorkload = particles > 2000 ? WorkloadLevel.HIGH
                : particles > 500 ? WorkloadLevel.MEDIUM
                : WorkloadLevel.LOW;

        // Memory workload: JVM usage ratio
        double usageRatio = memoryEngine.getUsageRatio();
        memoryWorkload = usageRatio > 0.85 ? WorkloadLevel.HIGH
                : usageRatio > 0.65 ? WorkloadLevel.MEDIUM
                : WorkloadLevel.LOW;
    }

    // ── Diagnosis ─────────────────────────────────────────────────────────────

    /**
     * Produces a diagnostic event for an observed FPS drop.
     * Uses measured workload levels to assign confidence levels to probable causes.
     * <b>Never claims 100 % certainty for unmeasured causes.</b>
     */
    private DropEvent diagnose(int prevFps, int currFps) {
        DropEvent event = new DropEvent(prevFps, currFps);

        event.addCause("Chunk workload", chunkWorkload,
                chunkWorkload == WorkloadLevel.HIGH ? Confidence.LIKELY : Confidence.POSSIBLE);
        event.addCause("Entity rendering", entityWorkload,
                entityWorkload == WorkloadLevel.HIGH ? Confidence.LIKELY : Confidence.POSSIBLE);
        event.addCause("Particle workload", particleWorkload,
                particleWorkload == WorkloadLevel.HIGH ? Confidence.LIKELY : Confidence.POSSIBLE);
        event.addCause("Memory pressure", memoryWorkload,
                memoryWorkload == WorkloadLevel.HIGH ? Confidence.HIGHLY_LIKELY : Confidence.POSSIBLE);

        // Frame-time spikes are a measured fact
        if (frameTimeMonitor.hadRecentSpike(3000)) {
            event.addCause("Frame-time spike", WorkloadLevel.HIGH, Confidence.CONFIRMED);
        }

        // Determine primary cause (highest confidence + highest workload)
        event.primaryCause = event.causes.stream()
                .filter(c -> c.workload == WorkloadLevel.HIGH || c.workload == WorkloadLevel.MEDIUM)
                .max(java.util.Comparator.comparingInt(c -> c.confidence.ordinal()))
                .map(c -> c.label)
                .orElse("Unknown");

        return event;
    }

    // ── Auto-optimize ─────────────────────────────────────────────────────────

    /**
     * Applies conservative automatic optimizations based on the drop event.
     * Only modifies settings that are safe to change at runtime.
     * All changes are logged and can be reverted.
     */
    private void applyAutoOptimize(DropEvent event) {
        boolean changed = false;

        if (chunkWorkload == WorkloadLevel.HIGH
                && config.chunkSchedule != PulseConfig.ChunkSchedule.CONSERVATIVE) {
            PulseConfig.ChunkSchedule prev = config.chunkSchedule;
            config.chunkSchedule = PulseConfig.ChunkSchedule.BALANCED;
            PulseOptimize.LOGGER.info("[PulseOptimize] Auto-optimize: ChunkSchedule {} -> {}",
                    prev, config.chunkSchedule);
            changed = true;
        }

        if (particleWorkload == WorkloadLevel.HIGH) {
            if (config.explosion_particles == PulseConfig.ParticleLevel.NORMAL) {
                config.explosion_particles = PulseConfig.ParticleLevel.REDUCED;
                PulseOptimize.LOGGER.info("[PulseOptimize] Auto-optimize: explosion_particles -> REDUCED");
                changed = true;
            }
        }

        if (changed) {
            config.save();
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public DropEvent getLastDropEvent() { return lastDropEvent; }
    public List<DropEvent> getDropHistory() { return java.util.Collections.unmodifiableList(dropHistory); }
    public WorkloadLevel getChunkWorkload() { return chunkWorkload; }
    public WorkloadLevel getEntityWorkload() { return entityWorkload; }
    public WorkloadLevel getParticleWorkload() { return particleWorkload; }
    public WorkloadLevel getMemoryWorkload() { return memoryWorkload; }

    /** Human-readable status string for the HUD. */
    public String getStatusLabel() {
        if (safeMode.isActive()) return "SAFE MODE";
        int fps = fpsMonitor.getCurrentFps();
        if (fps <= 0) return "MEASURING";
        if (frameTimeMonitor.hadRecentSpike(5000)) return "UNSTABLE";
        if (fps < 30) return "LOW FPS";
        if (fps < 60) return "MODERATE";
        return "SMOOTH";
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public enum WorkloadLevel { LOW, MEDIUM, HIGH, UNKNOWN }

    public enum Confidence {
        UNKNOWN, POSSIBLE, LIKELY, HIGHLY_LIKELY, CONFIRMED;

        public String label() {
            return switch (this) {
                case CONFIRMED -> "Confirmed";
                case HIGHLY_LIKELY -> "Highly likely";
                case LIKELY -> "Likely";
                case POSSIBLE -> "Possible";
                default -> "Unknown";
            };
        }
    }

    public static class CauseEntry {
        public final String label;
        public final WorkloadLevel workload;
        public final Confidence confidence;

        CauseEntry(String label, WorkloadLevel workload, Confidence confidence) {
            this.label = label;
            this.workload = workload;
            this.confidence = confidence;
        }
    }

    public static class DropEvent {
        public final int previousFps;
        public final int currentFps;
        public final long timestamp;
        public final List<CauseEntry> causes = new ArrayList<>();
        public String primaryCause = "Unknown";

        DropEvent(int previousFps, int currentFps) {
            this.previousFps = previousFps;
            this.currentFps = currentFps;
            this.timestamp = System.currentTimeMillis();
        }

        void addCause(String label, WorkloadLevel workload, Confidence confidence) {
            causes.add(new CauseEntry(label, workload, confidence));
        }
    }
}
