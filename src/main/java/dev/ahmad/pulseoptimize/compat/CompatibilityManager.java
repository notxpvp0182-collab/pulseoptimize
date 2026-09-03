package dev.ahmad.pulseoptimize.compat;

import dev.ahmad.pulseoptimize.PulseOptimize;
import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Detects installed performance-related Fabric mods and reports compatibility status.
 * <p>
 * This is <em>passive</em> detection only — we read the mod list from FabricLoader and
 * never call into other mods' internals, modify their mixins, or disable their features.
 * When a compatible mod is detected, PulseOptimize may disable its own overlapping
 * features to avoid duplication, but only where it is safe and simple to do so.
 */
public class CompatibilityManager {

    public enum Status { SAFE, LIMITED, UNKNOWN }

    public record ModCompat(String displayName, Status status, String note) {}

    /** Mod ID → compatibility record for all detected relevant mods. */
    private final Map<String, ModCompat> detected = new LinkedHashMap<>();

    /**
     * Runs the detection pass. Called once during mod initialisation.
     */
    public void detect() {
        checkMod("sodium",          "Sodium",          Status.SAFE,
                "Sodium handles most rendering. PulseOptimize particle/animation controls remain active.");
        checkMod("lithium",         "Lithium",          Status.SAFE,
                "Lithium optimises server-side logic. No conflicts expected.");
        checkMod("krypton",         "Krypton",          Status.SAFE,
                "Krypton optimises networking. No conflicts expected.");
        checkMod("ferrite-core",    "FerriteCore",      Status.SAFE,
                "FerriteCore reduces memory use. PulseOptimize memory monitoring remains active.");
        checkMod("immediatelyfast", "ImmediatelyFast",  Status.SAFE,
                "ImmediatelyFast optimises immediate-mode rendering. No conflicts expected.");
        checkMod("modernfix",       "ModernFix",        Status.SAFE,
                "ModernFix provides miscellaneous fixes. No conflicts expected.");
        checkMod("entity_culling",  "Entity Culling",   Status.LIMITED,
                "Entity Culling already handles entity culling. PulseOptimize culling is auto-disabled.");
        checkMod("indium",          "Indium",           Status.SAFE,
                "Indium provides Sodium compatibility. No conflicts expected.");
        checkMod("chunky",          "Chunky",           Status.SAFE,
                "Chunky handles world pre-generation. PulseOptimize will not conflict with generation tasks.");

        if (!detected.isEmpty()) {
            PulseOptimize.LOGGER.info("[PulseOptimize] Compatibility: detected {} mod(s) — {}",
                    detected.size(), getSummary());
        }
    }

    private void checkMod(String modId, String displayName, Status status, String note) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            detected.put(modId, new ModCompat(displayName, status, note));
            PulseOptimize.LOGGER.info("[PulseOptimize] Detected mod: {} — status: {}", displayName, status);
        }
    }

    /** Returns {@code true} if the given mod ID is detected. */
    public boolean isLoaded(String modId) {
        return detected.containsKey(modId);
    }

    /** Returns the compatibility record for a mod, or {@code null} if not detected. */
    public ModCompat getCompat(String modId) {
        return detected.get(modId);
    }

    /** All detected mods, in detection order. */
    public Map<String, ModCompat> getAllDetected() {
        return java.util.Collections.unmodifiableMap(detected);
    }

    /**
     * Returns a brief comma-separated summary of detected mod display names,
     * or "none" if none were found.
     */
    public String getSummary() {
        if (detected.isEmpty()) return "none";
        return String.join(", ", detected.values().stream()
                .map(ModCompat::displayName).toList());
    }
}
