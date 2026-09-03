package dev.ahmad.pulseoptimize.engine;

/**
 * Monitors JVM heap usage and provides memory pressure estimates.
 * <p>
 * Does <em>not</em> call {@code System.gc()} repeatedly — garbage collection is
 * left entirely to the JVM. The engine only provides observational data and
 * encourages the mod's other systems to avoid unnecessary allocations.
 */
public class MemoryEngine {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    /** Bytes per megabyte. */
    private static final long MB = 1024L * 1024L;

    // Cached readings updated on demand (called from the perf engine every second)
    private long usedMb = 0;
    private long allocatedMb = 0;
    private long maxMb = 0;
    private double usageRatio = 0.0;

    /**
     * Refreshes the cached memory statistics from the JVM runtime.
     * Called periodically — not every frame.
     */
    public void refresh() {
        long total = RUNTIME.totalMemory();
        long free = RUNTIME.freeMemory();
        long max = RUNTIME.maxMemory();

        usedMb = (total - free) / MB;
        allocatedMb = total / MB;
        maxMb = max == Long.MAX_VALUE ? -1L : max / MB;

        long usableMax = (maxMb > 0) ? maxMb : allocatedMb;
        usageRatio = usableMax > 0 ? (double) usedMb / usableMax : 0.0;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Used heap in MB. */
    public long getUsedMb() { refresh(); return usedMb; }

    /** Allocated (committed) heap in MB. */
    public long getAllocatedMb() { return allocatedMb; }

    /** Maximum heap in MB, or -1 if unbounded. */
    public long getMaxMb() { return maxMb; }

    /**
     * Heap usage ratio (0.0–1.0). Values above 0.85 are considered high pressure.
     * Callers should call {@link #refresh()} before reading this if they need a
     * fresh value.
     */
    public double getUsageRatio() { return usageRatio; }

    /** Returns a human-readable pressure label. */
    public String getPressureLabel() {
        if (usageRatio > 0.85) return "HIGH";
        if (usageRatio > 0.65) return "MEDIUM";
        return "LOW";
    }

    /** Returns a human-readable status label. */
    public String getStatusLabel() {
        if (usageRatio > 0.90) return "CRITICAL";
        if (usageRatio > 0.80) return "HIGH";
        if (usageRatio > 0.65) return "ELEVATED";
        return "STABLE";
    }
}
