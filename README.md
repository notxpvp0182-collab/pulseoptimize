# PulseOptimize

**An all-in-one Minecraft performance optimisation suite for Fabric.**

> Author: Ahmad  
> Target: Minecraft 1.21.1 · Fabric · Java 21

---

## What it does

PulseOptimize continuously monitors your client-side performance and provides
intelligent diagnostics, configurable visual optimisations, and an optional
AI-powered advisor — all without making unrealistic promises about FPS.

### Core features

| Feature | Description |
|---|---|
| **Performance HUD** | Live FPS, 1 % low, 0.1 % low, frame time and RAM overlay |
| **FPS Drop Detection** | Detects drops and captures a diagnostic snapshot with probable causes |
| **Auto-Optimize** | Conservatively adjusts settings on detected drops (with hysteresis) |
| **Memory Engine** | Monitors JVM heap usage and pressure without forcing GC |
| **Chunk Engine** | Tracks pending chunk workload for diagnostics |
| **Entity Culling** | Skips rendering of off-screen entities |
| **Particle Control** | Per-category particle levels: OFF / LOW / REDUCED / NORMAL |
| **Animation Control** | Toggle water, lava, fire, portal, enchantment, terrain, weather animations |
| **Low Fire** | Reduced fire overlay height / opacity |
| **Water Optimisation** | Clear water, reduced fog, reduced underwater particles |
| **Explosion Reduction** | Reduce explosion particles, smoke, debris and screen effects |
| **Combat / PvP** | Crystal rendering, particle and animation reduction (visual only) |
| **Environment** | Fog, rain, snow, cloud, portal and potion effect optimisation |
| **Diagnostics Screen** | Full live diagnostic view with workload estimates and drop history |
| **Compatibility Manager** | Detects Sodium, Lithium, FerriteCore, Entity Culling, etc. |
| **Safe Mode** | Automatically disables a feature if it causes instability |
| **AI Advisor** | Optional — bring your own OpenRouter or Gemini API key |

---

## Installation

1. Install [Fabric Loader 0.16+](https://fabricmc.net/use/installer/) for Minecraft 1.21.1.
2. Install [Fabric API 0.102+](https://modrinth.com/mod/fabric-api).
3. Install [Mod Menu 11+](https://modrinth.com/mod/modmenu) (optional, for the config screen).
4. Drop the PulseOptimize JAR into your `mods/` folder.

---

## Configuration

Open the config screen via **Mod Menu → PulseOptimize → Config**.

Configuration is saved to `.minecraft/config/pulseoptimize.json`.

### Presets

| Preset | Description |
|---|---|
| Default | Vanilla-close, minimal changes |
| Balanced | Safe optimisations enabled |
| Performance | More aggressive visual reductions |
| Maximum Performance | All visual reductions active |
| PvP | Crystal, fire and particle optimisations |
| Survival | Entity culling + chunk engine |
| Custom | Your own settings |

---

## Keybinds

All keybinds are **unbound by default**. Assign them in Options → Controls → PulseOptimize.

| Action | Default |
|---|---|
| Toggle HUD | Unbound |
| Open Diagnostics | Unbound |
| Toggle Auto-Optimize | Unbound |

---

## AI Advisor (optional)

The mod works **completely without AI**. If you want AI-assisted recommendations:

1. Open **Config → AI Advisor**.
2. Select provider: OpenRouter or Gemini.
3. Enter your own API key (never stored in plain logs).
4. Enter your model name (e.g. `openai/gpt-4o-mini`).
5. Press **Test Connection**.
6. Enable the advisor.

> **API keys are never hard-coded, never logged, and never shared.**  
> Model availability and pricing depend on your provider account.

The AI receives a sanitised, anonymous performance snapshot — no world data,
no player names, no server addresses. All recommended changes require your
manual confirmation before being applied.

---

## Compatibility

PulseOptimize detects and coexists with:

- Sodium · Lithium · Krypton · FerriteCore · ImmediatelyFast
- ModernFix · Entity Culling · Indium · Chunky

When a compatible mod is detected, overlapping PulseOptimize features are
disabled automatically to avoid duplication.

---

## Building from source

```bash
git clone https://github.com/ahmad/pulseoptimize
cd pulseoptimize
./gradlew build
# JAR is in build/libs/
```

Requires Java 21.

---

## Honesty policy

PulseOptimize will never:
- Claim it can guarantee a specific FPS number
- Display fake benchmark results
- Access network resources without explicit user configuration
- Modify server-side mechanics, hitboxes or reach
- Include or expose API keys in any release build
- Attribute performance improvements to placebo features

---

## License

MIT — see [LICENSE](LICENSE).

Author: **Ahmad**
