package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Shared base for all PulseOptimize sub-configuration screens.
 * <p>
 * Provides a consistent title bar and "Done" button. Sub-classes call
 * {@link #addToggle}, {@link #addCycleButton} and similar helpers to build
 * their UI without duplicating boilerplate.
 */
public abstract class BaseConfigScreen extends Screen {

    protected final Screen parent;
    protected final PulseConfig config;

    /** Y position for the next widget, incremented by each helper. */
    protected int nextY;

    protected static final int BTN_W = 200;
    protected static final int BTN_H = 20;
    protected static final int GAP   = 24;

    protected BaseConfigScreen(Screen parent, PulseConfig config, String titleKey) {
        super(Text.translatable(titleKey));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        nextY = 40;
        buildContent();

        // Done button at the bottom
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"),
                btn -> close())
                .dimensions(this.width / 2 - 75, this.height - 28, 150, BTN_H)
                .build());
    }

    /** Sub-classes implement this to add their widgets. */
    protected abstract void buildContent();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    // ── Widget helpers ────────────────────────────────────────────────────────

    /**
     * Adds a toggle button that shows the current boolean state.
     *
     * @param labelKey  translation key for the label
     * @param getter    current value supplier
     * @param setter    value consumer
     */
    protected void addToggle(String labelKey, java.util.function.BooleanSupplier getter,
                              java.util.function.Consumer<Boolean> setter) {
        int cx = this.width / 2;
        ButtonWidget btn = ButtonWidget.builder(
                buildToggleText(labelKey, getter.getAsBoolean()),
                b -> {
                    boolean newVal = !getter.getAsBoolean();
                    setter.accept(newVal);
                    b.setMessage(buildToggleText(labelKey, newVal));
                    config.save();
                })
                .dimensions(cx - BTN_W / 2, nextY, BTN_W, BTN_H)
                .build();
        addDrawableChild(btn);
        nextY += GAP;
    }

    protected static Text buildToggleText(String labelKey, boolean value) {
        String stateStr = value ? "§aON§r" : "§cOFF§r";
        return Text.literal(Text.translatable(labelKey).getString() + ": " + stateStr);
    }

    /**
     * Adds a label-only row (no interaction).
     */
    protected void addLabel(String text) {
        // Labels are drawn in render() — store as a simple record
        // For now, use a disabled button as a label (common Minecraft pattern)
        int cx = this.width / 2;
        ButtonWidget label = ButtonWidget.builder(Text.literal("§e" + text), b -> {})
                .dimensions(cx - BTN_W / 2, nextY, BTN_W, BTN_H)
                .build();
        label.active = false;
        addDrawableChild(label);
        nextY += GAP;
    }
}
