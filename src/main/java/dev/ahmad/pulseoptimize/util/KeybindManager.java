package dev.ahmad.pulseoptimize.util;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {

    private KeyBinding toggleHud;
    private KeyBinding openDiagnostics;
    private KeyBinding toggleAutoOptimize;

    private static final KeyBinding.Category KEYBIND_CATEGORY =
            KeyBinding.Category.create("key.categories.pulseoptimize");

    public void register() {
        toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEYBIND_CATEGORY));

        openDiagnostics = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.open_diagnostics",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEYBIND_CATEGORY));

        toggleAutoOptimize = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.toggle_auto_optimize",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEYBIND_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(this::handleKeys);
    }

    private void handleKeys(MinecraftClient client) {
        if (toggleHud.wasPressed() && PulseOptimize.getConfig() != null) {
            PulseOptimize.getConfig().showHud = !PulseOptimize.getConfig().showHud;
            PulseOptimize.getConfig().save();
        }
        if (openDiagnostics.wasPressed() && client.currentScreen == null) {
            client.setScreen(new dev.ahmad.pulseoptimize.ui.DiagnosticsScreen(null));
        }
        if (toggleAutoOptimize.wasPressed() && PulseOptimize.getConfig() != null) {
            PulseOptimize.getConfig().autoOptimize = !PulseOptimize.getConfig().autoOptimize;
            PulseOptimize.getConfig().save();
        }
    }
}
