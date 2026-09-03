# Changelog

All notable changes to PulseOptimize will be documented here.

## [1.0.0] — Initial Release

### Added
- Performance HUD: FPS, 1 % low, 0.1 % low, frame time, RAM, status
- FPS Drop Detection with diagnostic snapshots and cause confidence levels
- Auto-Optimize with hysteresis (optional, disabled by default)
- Memory Engine: JVM heap monitoring without forced GC
- Chunk Engine: passive pending-chunk workload tracking
- Entity and block-entity culling toggle
- Per-category particle level control (OFF / LOW / REDUCED / NORMAL)
  - Explosion, Smoke, Fire, Water, Lava, Potion, Crit Hit, Enchantment,
    Block Break, Falling, Dripping, Portal, Ambient, Weather
- Animation toggles: Water, Lava, Fire, Portal, Enchantment, Terrain, Weather
- Low Fire overlay (height, scale, opacity configurable)
- Water optimisations: Clear Water, Fog Reduction, Underwater Particle Reduction
- Explosion optimisations: Particles, Smoke, Debris, Screen Effects
- Combat / PvP optimisations: Crystal rendering, particles, animation; item rendering
- Environment: Fog, Rain, Snow, Cloud, Portal Effect, Potion Effect
- Diagnostics screen with live workload estimates and drop history
- Safe Mode: auto-disables a feature on detected instability
- Compatibility Manager: detects Sodium, Lithium, Krypton, FerriteCore,
  ImmediatelyFast, ModernFix, Entity Culling, Indium, Chunky
- Presets: Default, Balanced, Performance, Maximum Performance, PvP, Survival
- Optional AI Advisor (OpenRouter + Gemini) with user-supplied keys
- Configurable keybinds for HUD toggle, Diagnostics, Auto-Optimize toggle
- Mod Menu integration
- Persistent JSON configuration
- GitHub Actions CI build
