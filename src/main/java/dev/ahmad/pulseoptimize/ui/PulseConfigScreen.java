package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Root configuration screen for PulseOptimize.
 * <p>
 * Displays a list of category buttons that open their respective sub-screens.
 * Uses vanilla Minecraft GUI widgets for full compatibility and the expected
 * Minecraft look-and-feel.
 */
public class PulseConfigScreen extends Screen {

    private final Screen parent;
    private final PulseConfig config;

    /** Currently open sub-screen (null = show root). */
    private Screen activeSubScreen = null;

    public PulseConfigScreen(Screen parent, PulseConfig config) {
        super(Text.translatable("pulseoptimize.config.title"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        int startY = 40;
        int btnW = 200;
        int btnH = 20;
        int gap = 24;

        // ── Category buttons ──────────────────────────────────────────────────

        addCategory(cx, startY,       btnW, btnH, "pulseoptimize.config.general",      this::openGeneral);
        addCategory(cx, startY+gap,   btnW, btnH, "pulseoptimize.config.particles",    this::openParticles);
        addCategory(cx, startY+gap*2, btnW, btnH, "pulseoptimize.config.animations",   this::openAnimations);
        addCategory(cx, startY+gap*3, btnW, btnH, "pulseoptimize.config.fire",         this::openFire);
        addCategory(cx, startY+gap*4, btnW, btnH, "pulseoptimize.config.water",        this::openWater);
        addCategory(cx, startY+gap*5, btnW, btnH, "pulseoptimize.config.explosions",   this::openExplosions);
        addCategory(cx, startY+gap*6, btnW, btnH, "pulseoptimize.config.combat",       this::openCombat);
        addCategory(cx, startY+gap*7, btnW, btnH, "pulseoptimize.config.environment",  this::openEnvironment);
        addCategory(cx, startY+gap*8, btnW, btnH, "pulseoptimize.config.diagnostics",  this::openDiagnostics);
        addCategory(cx, startY+gap*9, btnW, btnH, "pulseoptimize.config.ai",           this::openAi);

        // ── Preset selector ───────────────────────────────────────────────────

        int presetY = this.height - 50;
        int pBtnW = 100;
        int presetStartX = cx - (pBtnW * 3 + 10);

        String[] presetLabels = {"Default", "Balanced", "Performance", "PvP", "Survival"};
        PulseConfig.Preset[] presets = {
            PulseConfig.Preset.DEFAULT, PulseConfig.Preset.BALANCED,
            PulseConfig.Preset.PERFORMANCE, PulseConfig.Preset.PVP,
            PulseConfig.Preset.SURVIVAL
        };

        for (int i = 0; i < presets.length; i++) {
            final PulseConfig.Preset p = presets[i];
            final String label = presetLabels[i];
            int px = presetStartX + i * (pBtnW + 4);
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> applyPreset(p))
                    .dimensions(px, presetY, pBtnW, 18)
                    .build());
        }

        // Done button
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"), btn -> close())
                .dimensions(cx - 75, this.height - 28, 150, 20)
                .build());
    }

    private void addCategory(int cx, int y, int w, int h, String translationKey,
                              Runnable openAction) {
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(translationKey),
                btn -> openAction.run())
                .dimensions(cx - w / 2, y, w, h)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the sub-screen if one is open
        if (activeSubScreen != null) {
            activeSubScreen.render(context, mouseX, mouseY, delta);
            return;
        }

        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("pulseoptimize.config.title"),
                this.width / 2, 16, 0xFFFFFF);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Preset:"),
                this.width / 2, this.height - 62, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    // ── Sub-screen openers ────────────────────────────────────────────────────

    private void openGeneral() {
        if (this.client != null) {
            this.client.setScreen(new GeneralConfigScreen(this, config));
        }
    }

    private void openParticles() {
        if (this.client != null) {
            this.client.setScreen(new ParticleConfigScreen(this, config));
        }
    }

    private void openAnimations() {
        if (this.client != null) {
            this.client.setScreen(new AnimationConfigScreen(this, config));
        }
    }

    private void openFire() {
        if (this.client != null) {
            this.client.setScreen(new FireConfigScreen(this, config));
        }
    }

    private void openWater() {
        if (this.client != null) {
            this.client.setScreen(new WaterConfigScreen(this, config));
        }
    }

    private void openExplosions() {
        if (this.client != null) {
            this.client.setScreen(new ExplosionConfigScreen(this, config));
        }
    }

    private void openCombat() {
        if (this.client != null) {
            this.client.setScreen(new CombatConfigScreen(this, config));
        }
    }

    private void openEnvironment() {
        if (this.client != null) {
            this.client.setScreen(new EnvironmentConfigScreen(this, config));
        }
    }

    private void openDiagnostics() {
        if (this.client != null) {
            this.client.setScreen(new DiagnosticsScreen(this));
        }
    }

    private void openAi() {
        if (this.client != null) {
            this.client.setScreen(new AiConfigScreen(this, config));
        }
    }

    private void applyPreset(PulseConfig.Preset preset) {
        config.applyPreset(preset);
        // Rebuild the screen to reflect new values
        if (this.client != null) {
            this.clearChildren();
            this.init();
        }
    }
}
