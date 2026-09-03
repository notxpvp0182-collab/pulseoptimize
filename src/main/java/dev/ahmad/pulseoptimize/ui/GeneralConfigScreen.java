package dev.ahmad.pulseoptimize.ui;

import dev.ahmad.pulseoptimize.config.PulseConfig;
import net.minecraft.client.gui.screen.Screen;

/** General / global settings screen. */
public class GeneralConfigScreen extends BaseConfigScreen {

    public GeneralConfigScreen(Screen parent, PulseConfig config) {
        super(parent, config, "pulseoptimize.config.general");
    }

    @Override
    protected void buildContent() {
        addToggle("pulseoptimize.config.show_hud",
                () -> config.showHud,
                v -> config.showHud = v);

        addToggle("pulseoptimize.config.auto_optimize",
                () -> config.autoOptimize,
                v -> config.autoOptimize = v);

        addToggle("pulseoptimize.config.warnings_enabled",
                () -> config.warnings_enabled,
                v -> config.warnings_enabled = v);

        addToggle("pulseoptimize.config.memory_engine",
                () -> config.memoryEngine_enabled,
                v -> config.memoryEngine_enabled = v);

        addToggle("pulseoptimize.config.chunk_engine",
                () -> config.chunkEngine_enabled,
                v -> config.chunkEngine_enabled = v);

        addToggle("pulseoptimize.config.entity_culling",
                () -> config.entityCulling_enabled,
                v -> config.entityCulling_enabled = v);

        addToggle("pulseoptimize.config.block_entity_culling",
                () -> config.blockEntityCulling_enabled,
                v -> config.blockEntityCulling_enabled = v);
    }
}
