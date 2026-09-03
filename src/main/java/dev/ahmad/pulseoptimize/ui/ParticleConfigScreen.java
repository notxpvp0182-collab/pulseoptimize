package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Particle level configuration screen.
 * <p>
 * Each particle category has a cycle button: OFF → LOW → REDUCED → NORMAL → …
 */
public class ParticleConfigScreen extends BaseConfigScreen {

    public ParticleConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.particles");
    }

    @Override
    protected void buildContent() {
        addParticleCycle("Explosion",   () -> config.explosion_particles,   v -> config.explosion_particles   = v);
        addParticleCycle("Smoke",       () -> config.smoke_particles,       v -> config.smoke_particles       = v);
        addParticleCycle("Fire",        () -> config.fire_particles,        v -> config.fire_particles        = v);
        addParticleCycle("Water",       () -> config.water_particles,       v -> config.water_particles       = v);
        addParticleCycle("Lava",        () -> config.lava_particles,        v -> config.lava_particles        = v);
        addParticleCycle("Potion",      () -> config.potion_particles,      v -> config.potion_particles      = v);
        addParticleCycle("Crit Hit",    () -> config.critHit_particles,     v -> config.critHit_particles     = v);
        addParticleCycle("Enchantment", () -> config.enchantment_particles, v -> config.enchantment_particles = v);
        addParticleCycle("Block Break", () -> config.blockBreak_particles,  v -> config.blockBreak_particles  = v);
        addParticleCycle("Falling",     () -> config.falling_particles,     v -> config.falling_particles     = v);
        addParticleCycle("Dripping",    () -> config.dripping_particles,    v -> config.dripping_particles    = v);
        addParticleCycle("Portal",      () -> config.portal_particles,      v -> config.portal_particles      = v);
        addParticleCycle("Ambient",     () -> config.ambient_particles,     v -> config.ambient_particles     = v);
        addParticleCycle("Weather",     () -> config.weather_particles,     v -> config.weather_particles     = v);
    }

    private void addParticleCycle(String label,
                                   java.util.function.Supplier<PulseConfig.ParticleLevel> getter,
                                   java.util.function.Consumer<PulseConfig.ParticleLevel> setter) {
        int cx = this.width / 2;
        ButtonWidget btn = ButtonWidget.builder(
                buildCycleText(label, getter.get()),
                b -> {
                    PulseConfig.ParticleLevel next = cycleLevel(getter.get());
                    setter.accept(next);
                    b.setMessage(buildCycleText(label, next));
                    config.save();
                })
                .dimensions(cx - BTN_W / 2, nextY, BTN_W, BTN_H)
                .build();
        addDrawableChild(btn);
        nextY += GAP;
    }

    private static Text buildCycleText(String label, PulseConfig.ParticleLevel level) {
        String col = switch (level) {
            case OFF     -> "§c";
            case LOW     -> "§e";
            case REDUCED -> "§a";
            case NORMAL  -> "§f";
        };
        return Text.literal(label + ": " + col + level.name() + "§r");
    }

    private static PulseConfig.ParticleLevel cycleLevel(PulseConfig.ParticleLevel current) {
        PulseConfig.ParticleLevel[] vals = PulseConfig.ParticleLevel.values();
        return vals[(current.ordinal() + 1) % vals.length];
    }
}
