package dev.ahmad.pulseoptimize;

import dev.ahmad.pulseoptimize.ai.AiAdvisor;
import dev.ahmad.pulseoptimize.compat.CompatibilityManager;
import dev.ahmad.pulseoptimize.config.PulseConfig;
import dev.ahmad.pulseoptimize.engine.AnimationEngine;
import dev.ahmad.pulseoptimize.engine.ChunkEngine;
import dev.ahmad.pulseoptimize.engine.EntityEngine;
import dev.ahmad.pulseoptimize.engine.MemoryEngine;
import dev.ahmad.pulseoptimize.engine.ParticleEngine;
import dev.ahmad.pulseoptimize.engine.PerformanceEngine;
import dev.ahmad.pulseoptimize.engine.RenderEngine;
import dev.ahmad.pulseoptimize.engine.SafeMode;
import dev.ahmad.pulseoptimize.monitor.FpsMonitor;
import dev.ahmad.pulseoptimize.monitor.FrameTimeMonitor;
import dev.ahmad.pulseoptimize.render.HudRenderer;
import dev.ahmad.pulseoptimize.util.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PulseOptimize — client-side performance optimization suite for Minecraft 1.21.1 (Fabric).
 * <p>
 * Author: Ahmad
 */
@Environment(EnvType.CLIENT)
public class PulseOptimize implements ClientModInitializer {

    public static final String MOD_ID = "pulseoptimize";
    public static final String MOD_NAME = "PulseOptimize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Singleton subsystem references — accessed statically after init
    private static PulseConfig config;
    private static FpsMonitor fpsMonitor;
    private static FrameTimeMonitor frameTimeMonitor;
    private static MemoryEngine memoryEngine;
    private static ChunkEngine chunkEngine;
    private static RenderEngine renderEngine;
    private static EntityEngine entityEngine;
    private static ParticleEngine particleEngine;
    private static AnimationEngine animationEngine;
    private static PerformanceEngine performanceEngine;
    private static CompatibilityManager compatibilityManager;
    private static SafeMode safeMode;
    private static HudRenderer hudRenderer;
    private static KeybindManager keybindManager;
    private static AiAdvisor aiAdvisor;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing — version {}", MOD_NAME, getVersion());

        // 1. Load persistent configuration first
        config = PulseConfig.load();

        // 2. Compatibility detection (passive — reads FabricLoader mod list)
        compatibilityManager = new CompatibilityManager();
        compatibilityManager.detect();

        // 3. Safe mode controller
        safeMode = new SafeMode();

        // 4. Monitoring subsystems
        fpsMonitor = new FpsMonitor();
        frameTimeMonitor = new FrameTimeMonitor();
        memoryEngine = new MemoryEngine();

        // 5. Optimization subsystems
        chunkEngine = new ChunkEngine(config, safeMode);
        renderEngine = new RenderEngine(config, safeMode);
        entityEngine = new EntityEngine(config, safeMode);
        particleEngine = new ParticleEngine(config, safeMode);
        animationEngine = new AnimationEngine(config, safeMode);

        // 6. Master performance engine — wires the subsystems together
        performanceEngine = new PerformanceEngine(
                config, fpsMonitor, frameTimeMonitor, memoryEngine,
                chunkEngine, renderEngine, entityEngine, particleEngine, animationEngine,
                compatibilityManager, safeMode
        );
        performanceEngine.register();

        // 7. HUD renderer
        hudRenderer = new HudRenderer(fpsMonitor, frameTimeMonitor, memoryEngine, performanceEngine);
        hudRenderer.register();

        // 8. Keybinds
        keybindManager = new KeybindManager();
        keybindManager.register();

        // 9. AI advisor (optional, only active if user configures API key)
        aiAdvisor = new AiAdvisor(config, performanceEngine);

        LOGGER.info("[{}] Initialization complete. Safe mode: {}. Compat: {}",
                MOD_NAME, safeMode.isActive(), compatibilityManager.getSummary());
    }

    // ── Static accessors used by mixins and UI ────────────────────────────────

    public static PulseConfig getConfig() { return config; }
    public static FpsMonitor getFpsMonitor() { return fpsMonitor; }
    public static FrameTimeMonitor getFrameTimeMonitor() { return frameTimeMonitor; }
    public static MemoryEngine getMemoryEngine() { return memoryEngine; }
    public static ChunkEngine getChunkEngine() { return chunkEngine; }
    public static RenderEngine getRenderEngine() { return renderEngine; }
    public static EntityEngine getEntityEngine() { return entityEngine; }
    public static ParticleEngine getParticleEngine() { return particleEngine; }
    public static AnimationEngine getAnimationEngine() { return animationEngine; }
    public static PerformanceEngine getPerformanceEngine() { return performanceEngine; }
    public static CompatibilityManager getCompatibilityManager() { return compatibilityManager; }
    public static SafeMode getSafeMode() { return safeMode; }
    public static HudRenderer getHudRenderer() { return hudRenderer; }
    public static KeybindManager getKeybindManager() { return keybindManager; }
    public static AiAdvisor getAiAdvisor() { return aiAdvisor; }

    /**
     * Returns the mod version from FabricLoader metadata. Falls back to "unknown".
     */
    public static String getVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
