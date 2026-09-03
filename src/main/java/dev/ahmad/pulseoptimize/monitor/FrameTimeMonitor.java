package dev.ahmad.pulseoptimize.monitor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks per-frame render time in milliseconds and detects spikes.
 * <p>
 * A spike is recorded when the frame time exceeds the configured threshold
 * (default 33.3 ms ≈ 30 fps equivalent).
 */
public class FrameTimeMonitor {

    private static final int MAX_SAMPLES = 600; // ~10 s at 60 fps

    private final Deque<Double> samples = new ArrayDeque<>(MAX_SAMPLES);
    private double currentFrameTimeMs = 0.0;
    private double averageFrameTimeMs = 0.0;
    private int spikeCount = 0;
    private long lastSpikeTime = 0;

    /** Spike threshold in milliseconds. Configurable via config; default 33.3 ms. */
    private double spikeThresholdMs = 33.3;

    /**
     * Called once per rendered frame with the elapsed nanoseconds for that frame.
     */
    public void onFrame(long deltaNanos) {
        double ms = deltaNanos / 1_000_000.0;
        currentFrameTimeMs = ms;

        if (samples.size() >= MAX_SAMPLES) {
            samples.pollFirst();
        }
        samples.addLast(ms);

        // Check for spike
        if (ms > spikeThresholdMs) {
            spikeCount++;
            lastSpikeTime = System.currentTimeMillis();
        }

        // Rolling average — computed incrementally, affordable every frame
        double sum = 0;
        for (double s : samples) sum += s;
        averageFrameTimeMs = samples.isEmpty() ? 0 : sum / samples.size();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Frame time of the most recent frame in milliseconds. */
    public double getCurrentFrameTimeMs() { return currentFrameTimeMs; }

    /** Rolling average frame time in milliseconds. */
    public double getAverageFrameTimeMs() { return averageFrameTimeMs; }

    /** Number of spikes recorded since monitoring began. */
    public int getSpikeCount() { return spikeCount; }

    /** System time (ms) of the most recent spike, or 0 if none recorded. */
    public long getLastSpikeTime() { return lastSpikeTime; }

    /** Returns {@code true} if a spike was detected in the last {@code windowMs} milliseconds. */
    public boolean hadRecentSpike(long windowMs) {
        return lastSpikeTime > 0 && (System.currentTimeMillis() - lastSpikeTime) < windowMs;
    }

    public void setSpikeThresholdMs(double thresholdMs) {
        this.spikeThresholdMs = thresholdMs;
    }

    public double getSpikeThresholdMs() { return spikeThresholdMs; }
}
