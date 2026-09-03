package dev.ahmad.pulseoptimize.render;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.engine.MemoryEngine;
import dev.ahmad.pulseoptimize.engine.PerformanceEngine;
import dev.ahmad.pulseoptimize.monitor.FpsMonitor;
import dev.ahmad.pulseoptimize.monitor.FrameTimeMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders the optional performance HUD overlay.
 * <p>
 * The HUD is drawn after the vanilla HUD via {@code MixinInGameHud} and
 * uses vanilla {@code DrawContext} text rendering for full compatibility.
 * <p>
 * Example display:
 * <pre>
 * PULSEOPTIMIZE
 * FPS:       165
 * 1% LOW:    102
 * 0.1% LOW:   81
 * FRAME TIME: 6.0 ms
 * RAM:     4.1 GB
 * STATUS: SMOOTH
 * </pre>
 */
public class HudRenderer {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int BG_COLOR   = 0x88000000;
    private static final int PADDING    = 4;
    private static final int LINE_H     = 10;

    private final FpsMonitor fpsMonitor;
    private final FrameTimeMonitor frameTimeMonitor;
    private final MemoryEngine memoryEngine;
    private final PerformanceEngine performanceEngine;

    public HudRenderer(FpsMonitor fpsMonitor, FrameTimeMonitor frameTimeMonitor,
                       MemoryEngine memoryEngine, PerformanceEngine performanceEngine) {
        this.fpsMonitor = fpsMonitor;
        this.frameTimeMonitor = frameTimeMonitor;
        this.memoryEngine = memoryEngine;
        this.performanceEngine = performanceEngine;
    }

    /** Called by the Fabric event system — no-op here; registration is via mixin. */
    public void register() { /* injection done via MixinInGameHud */ }

    /**
     * Renders the HUD. Called from the mixin after vanilla HUD rendering.
     */
    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.debugEnabled) return; // don't duplicate F3

        String[] lines = buildLines();
        int width  = longestLineWidth(client, lines) + PADDING * 2;
        int height = lines.length * LINE_H + PADDING * 2;
        int x = PADDING;
        int y = PADDING;

        // Semi-transparent background
        context.fill(x, y, x + width, y + height, BG_COLOR);

        // Text lines
        for (int i = 0; i < lines.length; i++) {
            context.drawText(client.textRenderer, lines[i],
                    x + PADDING, y + PADDING + i * LINE_H, TEXT_COLOR, false);
        }
    }

    private String[] buildLines() {
        memoryEngine.refresh();
        return new String[]{
            "§bPULSEOPTIMIZE",
            String.format("FPS:        %d", fpsMonitor.getCurrentFps()),
            String.format("1%% LOW:     %d", fpsMonitor.get1PercentLow()),
            String.format("0.1%% LOW:   %d", fpsMonitor.get01PercentLow()),
            String.format("FRAME TIME: %.1f ms", frameTimeMonitor.getCurrentFrameTimeMs()),
            String.format("RAM:        %.1f GB", memoryEngine.getUsedMb() / 1024.0),
            String.format("STATUS:     %s", performanceEngine.getStatusLabel()),
        };
    }

    private int longestLineWidth(MinecraftClient client, String[] lines) {
        int max = 0;
        for (String line : lines) {
            int w = client.textRenderer.getWidth(line);
            if (w > max) max = w;
        }
        return max;
    }
}
