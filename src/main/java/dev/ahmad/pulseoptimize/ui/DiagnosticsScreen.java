package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.engine.PerformanceEngine;
import dev.ahmad.pulseoptimize.engine.PerformanceEngine.DropEvent;
import dev.ahmad.pulseoptimize.engine.MemoryEngine;
import dev.ahmad.pulseoptimize.engine.SafeMode;
import dev.ahmad.pulseoptimize.monitor.FpsMonitor;
import dev.ahmad.pulseoptimize.monitor.FrameTimeMonitor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Diagnostics centre — shows current performance metrics, the most recent
 * FPS drop event with attributed causes, and safe mode status.
 */
public class DiagnosticsScreen extends Screen {

    private final Screen parent;

    public DiagnosticsScreen(Screen parent) {
        super(Text.translatable("pulseoptimize.diagnostics.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"),
                btn -> close())
                .dimensions(this.width / 2 - 75, this.height - 28, 150, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);

        int x = 20;
        int y = 24;
        int lineH = 10;

        FpsMonitor fps = PulseOptimize.getFpsMonitor();
        FrameTimeMonitor ft = PulseOptimize.getFrameTimeMonitor();
        MemoryEngine mem = PulseOptimize.getMemoryEngine();
        PerformanceEngine perf = PulseOptimize.getPerformanceEngine();
        SafeMode safe = PulseOptimize.getSafeMode();

        if (fps == null || perf == null) {
            context.drawTextWithShadow(this.textRenderer,
                    "PulseOptimize not fully initialised.", x, y, 0xFF5555);
            return;
        }

        mem.refresh();

        // ── FPS block ─────────────────────────────────────────────────────────
        y = drawSection(context, "PERFORMANCE", x, y, lineH);
        y = drawLine(context, String.format("FPS: %d | Avg: %d | Min: %d",
                fps.getCurrentFps(), fps.getAverageFps(), fps.getMinFps()), x, y, lineH, 0xFFFFFF);
        y = drawLine(context, String.format("1%% Low: %d | 0.1%% Low: %d",
                fps.get1PercentLow(), fps.get01PercentLow()), x, y, lineH, 0xFFFFFF);
        y = drawLine(context, String.format("Frame Time: %.1f ms | Avg: %.1f ms",
                ft.getCurrentFrameTimeMs(), ft.getAverageFrameTimeMs()), x, y, lineH, 0xFFFFFF);
        y = drawLine(context, "Status: " + perf.getStatusLabel(), x, y, lineH, 0xAAFFAA);

        // ── Memory block ──────────────────────────────────────────────────────
        y += lineH;
        y = drawSection(context, "MEMORY", x, y, lineH);
        y = drawLine(context, String.format("Used: %d MB | Allocated: %d MB",
                mem.getUsedMb(), mem.getAllocatedMb()), x, y, lineH, 0xFFFFFF);
        y = drawLine(context, "Pressure: " + mem.getPressureLabel()
                + " | Status: " + mem.getStatusLabel(), x, y, lineH, 0xFFFFFF);

        // ── Workload block ────────────────────────────────────────────────────
        y += lineH;
        y = drawSection(context, "WORKLOAD ESTIMATES", x, y, lineH);
        y = drawLine(context, "Chunks:   " + perf.getChunkWorkload().name(), x, y, lineH, workloadColor(perf.getChunkWorkload()));
        y = drawLine(context, "Entities: " + perf.getEntityWorkload().name(), x, y, lineH, workloadColor(perf.getEntityWorkload()));
        y = drawLine(context, "Particles:" + perf.getParticleWorkload().name(), x, y, lineH, workloadColor(perf.getParticleWorkload()));
        y = drawLine(context, "Memory:   " + perf.getMemoryWorkload().name(), x, y, lineH, workloadColor(perf.getMemoryWorkload()));

        // ── Last drop event ───────────────────────────────────────────────────
        DropEvent drop = perf.getLastDropEvent();
        if (drop != null) {
            y += lineH;
            y = drawSection(context, "LAST FPS DROP", x, y, lineH);
            y = drawLine(context, String.format("FPS: %d -> %d", drop.previousFps, drop.currentFps),
                    x, y, lineH, 0xFF5555);
            y = drawLine(context, "Primary cause: " + drop.primaryCause, x, y, lineH, 0xFFAA00);
            for (var cause : drop.causes) {
                y = drawLine(context, String.format("  %s: %s (%s)",
                        cause.label, cause.workload.name(), cause.confidence.label()),
                        x, y, lineH, 0xCCCCCC);
            }
        }

        // ── Safe mode ─────────────────────────────────────────────────────────
        if (safe != null && safe.isActive()) {
            y += lineH;
            y = drawSection(context, "SAFE MODE ACTIVE", x, y, lineH);
            for (SafeMode.SafeModeEvent evt : safe.getEvents()) {
                y = drawLine(context, evt.featureName + ": " + evt.reason, x, y, lineH, 0xFF5555);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int drawSection(DrawContext ctx, String title, int x, int y, int lh) {
        ctx.drawTextWithShadow(this.textRenderer, "§b── " + title + " ──", x, y, 0x55FFFF);
        return y + lh + 2;
    }

    private int drawLine(DrawContext ctx, String text, int x, int y, int lh, int color) {
        ctx.drawTextWithShadow(this.textRenderer, text, x, y, color);
        return y + lh;
    }

    private static int workloadColor(PerformanceEngine.WorkloadLevel level) {
        return switch (level) {
            case HIGH    -> 0xFF5555;
            case MEDIUM  -> 0xFFAA00;
            case LOW     -> 0x55FF55;
            default      -> 0xAAAAAA;
        };
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}
