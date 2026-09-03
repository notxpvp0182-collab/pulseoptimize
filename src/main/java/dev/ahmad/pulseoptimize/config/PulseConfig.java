package dev.ahmad.pulseoptimize.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.ahmad.pulseoptimize.PulseOptimize;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent configuration for PulseOptimize.
 * <p>
 * Uses Gson for JSON serialisation. All fields have sensible defaults so a missing
 * or incomplete config file is always handled safely.
 */
public class PulseConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "pulseoptimize.json";

    // ── General ───────────────────────────────────────────────────────────────
    public boolean showHud = true;
    public boolean autoOptimize = false;
    public Preset preset = Preset.BALANCED;

    // ── FPS Monitor ───────────────────────────────────────────────────────────
    public int fpsHistorySize = 600; // 10 seconds at 60 fps
    public int fpsSampleWindowMs = 1000;
    public double fpsDrop_threshold = 0.35; // 35 % drop triggers detection

    // ── Frame Time ────────────────────────────────────────────────────────────
    public double frametime_spike_threshold_ms = 33.3; // ~30 fps equivalent spike

    // ── Memory Engine ─────────────────────────────────────────────────────────
    public boolean memoryEngine_enabled = true;
    public int memoryCheck_intervalSeconds = 10;

    // ── Chunk Engine ──────────────────────────────────────────────────────────
    public boolean chunkEngine_enabled = true;
    public ChunkSchedule chunkSchedule = ChunkSchedule.BALANCED;

    // ── Rendering ─────────────────────────────────────────────────────────────
    public boolean entityCulling_enabled = true;
    public boolean blockEntityCulling_enabled = true;

    // ── Particles ─────────────────────────────────────────────────────────────
    public ParticleLevel explosion_particles = ParticleLevel.NORMAL;
    public ParticleLevel smoke_particles = ParticleLevel.NORMAL;
    public ParticleLevel fire_particles = ParticleLevel.NORMAL;
    public ParticleLevel water_particles = ParticleLevel.NORMAL;
    public ParticleLevel lava_particles = ParticleLevel.NORMAL;
    public ParticleLevel potion_particles = ParticleLevel.NORMAL;
    public ParticleLevel critHit_particles = ParticleLevel.NORMAL;
    public ParticleLevel enchantment_particles = ParticleLevel.NORMAL;
    public ParticleLevel blockBreak_particles = ParticleLevel.NORMAL;
    public ParticleLevel falling_particles = ParticleLevel.NORMAL;
    public ParticleLevel dripping_particles = ParticleLevel.NORMAL;
    public ParticleLevel portal_particles = ParticleLevel.NORMAL;
    public ParticleLevel ambient_particles = ParticleLevel.NORMAL;
    public ParticleLevel weather_particles = ParticleLevel.NORMAL;

    // ── Animations ────────────────────────────────────────────────────────────
    public boolean water_animation = true;
    public boolean lava_animation = true;
    public boolean fire_animation = true;
    public boolean portal_animation = true;
    public boolean enchantment_animation = true;
    public boolean terrain_animation = true;
    public boolean weather_animation = true;

    // ── Low Fire ──────────────────────────────────────────────────────────────
    public boolean lowFire_enabled = false;
    public float lowFire_height = 0.3f;   // 0.0–1.0
    public float lowFire_scale = 1.0f;    // 0.5–1.0
    public float lowFire_opacity = 0.8f;  // 0.0–1.0

    // ── Water ─────────────────────────────────────────────────────────────────
    public boolean clearWater_enabled = false;
    public boolean waterFog_reduced = false;
    public boolean underwater_particles_reduced = false;

    // ── Explosions ────────────────────────────────────────────────────────────
    public boolean reduceExplosion_particles = false;
    public boolean reduceExplosion_smoke = false;
    public boolean reduceExplosion_debris = false;
    public boolean reduceExplosion_screenEffects = false;

    // ── Combat / PvP ─────────────────────────────────────────────────────────
    public boolean crystal_rendering_optimized = false;
    public boolean crystal_particles_reduced = false;
    public boolean crystal_animation_reduced = false;
    public boolean item_rendering_optimized = false;

    // ── Visual effects ────────────────────────────────────────────────────────
    public boolean fog_reduced = false;
    public boolean rain_optimized = false;
    public boolean snow_optimized = false;
    public boolean cloud_optimized = false;
    public boolean portalEffect_optimized = false;
    public boolean potionEffect_optimized = false;

    // ── AI Advisor ────────────────────────────────────────────────────────────
    public boolean ai_enabled = false;
    public AiProvider ai_provider = AiProvider.OPENROUTER;
    public String ai_apiKey = "";        // never logged, never hard-coded
    public String ai_model = "";
    public String ai_baseUrl = "";

    // ── Diagnostics ───────────────────────────────────────────────────────────
    public boolean warnings_enabled = true;
    public int warning_cooldown_seconds = 30;

    // ── Keybinds (LWJGL key codes stored as ints) ─────────────────────────────
    public int keybind_toggleHud = -1;          // GLFW_KEY_UNKNOWN = no bind
    public int keybind_openDiagnostics = -1;
    public int keybind_toggleAutoOptimize = -1;

    // ── Enumerations ──────────────────────────────────────────────────────────

    public enum Preset { DEFAULT, BALANCED, PERFORMANCE, MAXIMUM_PERFORMANCE, PVP, SURVIVAL, CUSTOM }

    public enum ChunkSchedule { CONSERVATIVE, BALANCED, AGGRESSIVE }

    public enum ParticleLevel { OFF, LOW, REDUCED, NORMAL }

    public enum AiProvider { OPENROUTER, GEMINI }

    // ── Persistence ───────────────────────────────────────────────────────────

    /**
     * Loads the config from disk. Returns a default config if the file does not exist or
     * is malformed, ensuring the mod always starts successfully.
     */
    public static PulseConfig load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                PulseConfig loaded = GSON.fromJson(reader, PulseConfig.class);
                if (loaded != null) {
                    PulseOptimize.LOGGER.info("[PulseOptimize] Configuration loaded from {}", path);
                    return loaded;
                }
            } catch (Exception e) {
                PulseOptimize.LOGGER.warn("[PulseOptimize] Failed to load config — using defaults. Cause: {}", e.getMessage());
            }
        }
        PulseConfig defaults = new PulseConfig();
        defaults.save(); // write defaults so the user can inspect the file
        return defaults;
    }

    /**
     * Saves the current configuration to disk.
     */
    public void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            PulseOptimize.LOGGER.error("[PulseOptimize] Could not create config directory: {}", e.getMessage());
            return;
        }
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            PulseOptimize.LOGGER.error("[PulseOptimize] Could not save config: {}", e.getMessage());
        }
    }

    /**
     * Applies a preset by setting relevant fields then saving.
     */
    public void applyPreset(Preset p) {
        this.preset = p;
        switch (p) {
            case PERFORMANCE -> {
                reduceExplosion_particles = true;
                reduceExplosion_smoke = true;
                fog_reduced = true;
                rain_optimized = true;
                snow_optimized = true;
                cloud_optimized = true;
                entityCulling_enabled = true;
                blockEntityCulling_enabled = true;
                chunkSchedule = ChunkSchedule.BALANCED;
                explosion_particles = ParticleLevel.REDUCED;
                smoke_particles = ParticleLevel.REDUCED;
                weather_particles = ParticleLevel.REDUCED;
            }
            case MAXIMUM_PERFORMANCE -> {
                reduceExplosion_particles = true;
                reduceExplosion_smoke = true;
                reduceExplosion_debris = true;
                reduceExplosion_screenEffects = true;
                fog_reduced = true;
                rain_optimized = true;
                snow_optimized = true;
                cloud_optimized = true;
                entityCulling_enabled = true;
                blockEntityCulling_enabled = true;
                chunkSchedule = ChunkSchedule.CONSERVATIVE;
                explosion_particles = ParticleLevel.LOW;
                smoke_particles = ParticleLevel.LOW;
                weather_particles = ParticleLevel.LOW;
                crystal_particles_reduced = true;
                crystal_animation_reduced = true;
                lowFire_enabled = true;
                water_animation = false;
                portal_animation = false;
            }
            case PVP -> {
                crystal_rendering_optimized = true;
                crystal_particles_reduced = true;
                crystal_animation_reduced = true;
                item_rendering_optimized = true;
                explosion_particles = ParticleLevel.LOW;
                entityCulling_enabled = true;
                fog_reduced = true;
                lowFire_enabled = true;
            }
            case SURVIVAL -> {
                entityCulling_enabled = true;
                blockEntityCulling_enabled = true;
                chunkSchedule = ChunkSchedule.BALANCED;
                rain_optimized = true;
                cloud_optimized = true;
            }
            default -> {
                // BALANCED / DEFAULT — leave most settings at defaults
                entityCulling_enabled = true;
                blockEntityCulling_enabled = true;
                chunkSchedule = ChunkSchedule.BALANCED;
            }
        }
        save();
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }
}
