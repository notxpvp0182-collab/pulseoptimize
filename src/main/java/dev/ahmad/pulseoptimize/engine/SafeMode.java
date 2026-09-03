package dev.ahmad.pulseoptimize.engine;

import dev.ahmad.pulseoptimize.PulseOptimize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Safe Mode controller.
 * <p>
 * When an optimisation subsystem encounters an unexpected error it should
 * report it here. SafeMode records the event, disables only the affected
 * feature and notifies the user — it does not touch unrelated settings.
 */
public class SafeMode {

    private boolean active = false;
    private final List<SafeModeEvent> events = new ArrayList<>();

    /** Records an instability event and marks the engine as active. */
    public void trigger(String featureName, String reason) {
        active = true;
        SafeModeEvent event = new SafeModeEvent(featureName, reason);
        events.add(event);
        PulseOptimize.LOGGER.warn(
                "[PulseOptimize] SAFE MODE ACTIVATED — Feature: {} | Reason: {}",
                featureName, reason);
    }

    /** Resets safe mode (e.g. after the user acknowledges and restarts). */
    public void reset() {
        active = false;
        events.clear();
    }

    public boolean isActive() { return active; }
    public List<SafeModeEvent> getEvents() { return Collections.unmodifiableList(events); }

    public static class SafeModeEvent {
        public final String featureName;
        public final String reason;
        public final long timestamp;

        SafeModeEvent(String featureName, String reason) {
            this.featureName = featureName;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
