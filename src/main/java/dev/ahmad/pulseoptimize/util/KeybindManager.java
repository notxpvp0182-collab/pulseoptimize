package dev.ahmad.pulseoptimize.util;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Registers and handles PulseOptimize keybindings.
 * <p>
 * Default bindings:
 * <ul>
 *   <li>Toggle HUD — unbound (user sets)</li>
 *   <li>Open Diagnostics — unbound</li>
 *   <li>Toggle Auto-Optimize — unbound</li>
 * </ul>
 */
public class KeybindManager {

    private static final String CATEGORY = "key.categories.pulseoptimize";

    private KeyBinding toggleHud;
    private KeyBinding openDiagnostics;
    private KeyBinding toggleAutoOptimize;

    /**
     * Registers keybinds with Fabric and attaches a tick listener to handle presses.
     */
    public void register() {
        toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY));

        openDiagnostics = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.open_diagnostics",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY));

        toggleAutoOptimize = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulseoptimize.toggle_auto_optimize",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY));

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
            PulseOptimize.LOGGER.info("[PulseOptimize] Auto-optimize: {}",
                    PulseOptimize.getConfig().autoOptimize ? "enabled" : "disabled");
        }
    }
}
