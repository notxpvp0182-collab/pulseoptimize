package dev.ahmad.pulseoptimize.monitor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks FPS using an efficient rolling circular buffer.
 * <p>
 * Statistics (average, 1 % low, 0.1 % low) are computed on demand via a
 * lightweight sorted-count approach — not every frame.
 */
public class FpsMonitor {

    /** Maximum number of FPS samples retained (10 seconds at 240 fps = 2 400). */
    private static final int MAX_SAMPLES = 2400;

    private final Deque<Integer> samples = new ArrayDeque<>(MAX_SAMPLES);
    private int currentFps = 0;
    private long lastFrameTime = System.nanoTime();
    private int frameCount = 0;
    private long windowStart = System.nanoTime();

    // Cached statistics — updated every ~250 ms
    private int cachedAvg = 0;
    private int cachedMin = 0;
    private int cached1pLow = 0;
    private int cached01pLow = 0;
    private long lastStatUpdate = 0;
    private static final long STAT_UPDATE_INTERVAL_MS = 250;

    /**
     * Called once per rendered frame. The caller (mixin) provides the elapsed
     * nanoseconds since the previous frame.
     *
     * @param deltaNanos nanoseconds elapsed since the previous frame
     */
    public void onFrame(long deltaNanos) {
        frameCount++;
        long now = System.nanoTime();

        // Update per-second FPS counter
        long windowElapsed = now - windowStart;
        if (windowElapsed >= 1_000_000_000L) {
            currentFps = frameCount;
            frameCount = 0;
            windowStart = now;

            // Record to rolling window
            if (samples.size() >= MAX_SAMPLES) {
                samples.pollFirst();
            }
            samples.addLast(currentFps);
        }

        // Lazily update expensive statistics
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastStatUpdate >= STAT_UPDATE_INTERVAL_MS && !samples.isEmpty()) {
            updateStatistics();
            lastStatUpdate = nowMs;
        }

        lastFrameTime = now;
    }

    /**
     * Computes average, min, 1 % low and 0.1 % low from the rolling window.
     * Uses a simple sort of the current snapshot — affordable at 250 ms intervals.
     */
    private void updateStatistics() {
        int[] arr = samples.stream().mapToInt(Integer::intValue).toArray();
        if (arr.length == 0) return;

        java.util.Arrays.sort(arr);

        // Average
        long sum = 0;
        for (int v : arr) sum += v;
        cachedAvg = (int) (sum / arr.length);

        // Min
        cachedMin = arr[0];

        // 1 % low = average of the bottom 1 % of frames
        int count1p = Math.max(1, arr.length / 100);
        long sum1p = 0;
        for (int i = 0; i < count1p; i++) sum1p += arr[i];
        cached1pLow = (int) (sum1p / count1p);

        // 0.1 % low = average of the bottom 0.1 % of frames
        int count01p = Math.max(1, arr.length / 1000);
        long sum01p = 0;
        for (int i = 0; i < count01p; i++) sum01p += arr[i];
        cached01pLow = (int) (sum01p / count01p);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** The most recent per-second FPS value. */
    public int getCurrentFps() { return currentFps; }

    /** Rolling-window average FPS. */
    public int getAverageFps() { return cachedAvg; }

    /** Rolling-window minimum FPS. */
    public int getMinFps() { return cachedMin; }

    /** 1 % low (average of bottom 1 % of samples). */
    public int get1PercentLow() { return cached1pLow; }

    /** 0.1 % low (average of bottom 0.1 % of samples). */
    public int get01PercentLow() { return cached01pLow; }

    /** Number of FPS samples currently held. */
    public int getSampleCount() { return samples.size(); }

    /**
     * Returns the previous rolling-average FPS before the most recent update — used
     * by the drop detector to compare before/after.
     */
    public int getPreviousFps() {
        if (samples.size() < 2) return currentFps;
        // Second-to-last entry is "previous second"
        return samples.stream().skip(samples.size() - 2L).findFirst().orElse(currentFps);
    }
}
