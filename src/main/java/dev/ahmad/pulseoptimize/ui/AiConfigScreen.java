package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.ai.AiAdvisor;
import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * AI Performance Advisor configuration screen.
 * <p>
 * API keys are masked in the text field. No key is ever logged or shown
 * in plain text outside of this screen.
 */
public class AiConfigScreen extends Screen {

    private final Screen parent;
    private final PulseConfig config;

    private TextFieldWidget apiKeyField;
    private TextFieldWidget modelField;
    private TextFieldWidget baseUrlField;
    private Text statusText = Text.literal("");

    public AiConfigScreen(Screen parent, PulseConfig config) {
        super(Text.translatable("pulseoptimize.config.ai"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;

        // Provider cycle button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Provider: " + config.ai_provider.name()),
                btn -> {
                    config.ai_provider = (config.ai_provider == PulseConfig.AiProvider.OPENROUTER)
                            ? PulseConfig.AiProvider.GEMINI
                            : PulseConfig.AiProvider.OPENROUTER;
                    btn.setMessage(Text.literal("Provider: " + config.ai_provider.name()));
                })
                .dimensions(cx - 100, y, 200, 20)
                .build());
        y += 28;

        // API Key field (masked)
        apiKeyField = new TextFieldWidget(this.textRenderer, cx - 100, y, 200, 18,
                Text.literal("API Key"));
        apiKeyField.setPlaceholder(Text.literal("Enter API key..."));
        apiKeyField.setText(config.ai_apiKey.isEmpty() ? "" : "••••••••");
        apiKeyField.setMaxLength(256);
        addDrawableChild(apiKeyField);
        y += 26;

        // Model field
        modelField = new TextFieldWidget(this.textRenderer, cx - 100, y, 200, 18,
                Text.literal("Model"));
        modelField.setPlaceholder(Text.literal("e.g. openai/gpt-4o-mini"));
        modelField.setText(config.ai_model);
        modelField.setMaxLength(128);
        addDrawableChild(modelField);
        y += 26;

        // Base URL field
        baseUrlField = new TextFieldWidget(this.textRenderer, cx - 100, y, 200, 18,
                Text.literal("Base URL"));
        baseUrlField.setPlaceholder(Text.literal("Optional custom base URL"));
        baseUrlField.setText(config.ai_baseUrl);
        baseUrlField.setMaxLength(256);
        addDrawableChild(baseUrlField);
        y += 30;

        // Enable toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("AI Advisor: " + (config.ai_enabled ? "§aON§r" : "§cOFF§r")),
                btn -> {
                    config.ai_enabled = !config.ai_enabled;
                    btn.setMessage(Text.literal("AI Advisor: " + (config.ai_enabled ? "§aON§r" : "§cOFF§r")));
                })
                .dimensions(cx - 100, y, 200, 20)
                .build());
        y += 28;

        // Test connection
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Test Connection"),
                btn -> testConnection())
                .dimensions(cx - 100, y, 200, 20)
                .build());
        y += 28;

        // Save
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.done"),
                btn -> save())
                .dimensions(cx - 75, this.height - 28, 150, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, statusText, cx, this.height - 50, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
                "§7Model availability and pricing depend on the provider/account.",
                cx - 140, this.height - 60, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "§7API Key:", cx - 100, 68, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "§7Model:", cx - 100, 94, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "§7Base URL (optional):", cx - 100, 120, 0xAAAAAA);
    }

    private void save() {
        // Only update key if it was changed (not still the masked placeholder)
        String keyInput = apiKeyField.getText();
        if (!keyInput.startsWith("••")) {
            config.ai_apiKey = keyInput;
        }
        config.ai_model = modelField.getText();
        config.ai_baseUrl = baseUrlField.getText();
        config.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    private void testConnection() {
        String keyInput = apiKeyField.getText();
        String key = keyInput.startsWith("••") ? config.ai_apiKey : keyInput;

        AiAdvisor advisor = PulseOptimize.getAiAdvisor();
        if (advisor == null) {
            statusText = Text.literal("§cAI advisor not initialised.");
            return;
        }

        statusText = Text.literal("§eTesting…");

        // Run on a background thread so the UI does not freeze
        Thread.ofVirtual().name("pulseoptimize-ai-test").start(() -> {
            boolean ok = advisor.testConnection(config.ai_provider, key,
                    modelField.getText(), baseUrlField.getText());
            statusText = ok
                    ? Text.literal("§aConnection successful.")
                    : Text.literal("§cConnection failed. Check key/model/URL.");
        });
    }
}
