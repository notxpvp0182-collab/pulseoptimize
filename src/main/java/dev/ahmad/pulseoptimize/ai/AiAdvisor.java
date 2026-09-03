package dev.ahmad.pulseoptimize.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.ahmad.pulseoptimize.PulseOptimize;
import dev.ahmad.pulseoptimize.config.PulseConfig;
import dev.ahmad.pulseoptimize.engine.PerformanceEngine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Optional AI Performance Advisor.
 * <p>
 * <b>The mod functions completely without this feature.</b> The AI advisor is
 * only active when the user has configured a valid API key and explicitly enabled
 * AI analysis.
 * <p>
 * Workflow:
 * <ol>
 *   <li>Collect local, sanitised performance data (no private world data).</li>
 *   <li>Send it to the configured provider (OpenRouter or Gemini).</li>
 *   <li>Parse the response into a list of {@link AiRecommendation} objects.</li>
 *   <li>Present recommendations to the user for manual approval.</li>
 *   <li>Apply only approved, safe configuration changes.</li>
 * </ol>
 * <p>
 * The AI never executes code, modifies files outside the config directory,
 * downloads mods, runs shell commands, or changes server/gameplay mechanics.
 */
public class AiAdvisor {

    private static final String OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String GEMINI_ENDPOINT_TPL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private static final Gson GSON = new Gson();
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final PulseConfig config;
    private final PerformanceEngine performanceEngine;
    private final HttpClient httpClient;

    public AiAdvisor(PulseConfig config, PerformanceEngine performanceEngine) {
        this.config = config;
        this.performanceEngine = performanceEngine;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the advisor is configured and enabled.
     */
    public boolean isAvailable() {
        return config.ai_enabled
                && config.ai_apiKey != null
                && !config.ai_apiKey.isBlank()
                && config.ai_model != null
                && !config.ai_model.isBlank();
    }

    /**
     * Tests connectivity to the configured provider with the supplied credentials.
     * Returns {@code true} on a successful (2xx) response, {@code false} otherwise.
     * <p>
     * This method blocks the calling thread — callers should run it off the main thread.
     */
    public boolean testConnection(PulseConfig.AiProvider provider, String apiKey,
                                  String model, String baseUrl) {
        try {
            String prompt = "Reply with the single word: OK";
            String response = sendRequest(provider, apiKey, model, baseUrl, prompt);
            return response != null && !response.isBlank();
        } catch (Exception e) {
            PulseOptimize.LOGGER.warn("[PulseOptimize] AI connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Collects a sanitised performance snapshot and requests analysis from the
     * configured AI provider.
     *
     * @return a list of recommendations (may be empty on failure)
     */
    public List<AiRecommendation> analysePerformance() {
        if (!isAvailable()) return List.of();

        String prompt = buildAnalysisPrompt();
        List<AiRecommendation> results = new ArrayList<>();

        try {
            String raw = sendRequest(config.ai_provider, config.ai_apiKey,
                    config.ai_model, config.ai_baseUrl, prompt);
            if (raw != null) {
                results = parseRecommendations(raw);
            }
        } catch (Exception e) {
            PulseOptimize.LOGGER.warn("[PulseOptimize] AI analysis failed: {}", e.getMessage());
        }

        return results;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a sanitised performance report prompt. No private world data,
     * no player identifiers, no server addresses are included.
     */
    private String buildAnalysisPrompt() {
        var fpsM = PulseOptimize.getFpsMonitor();
        var ftM  = PulseOptimize.getFrameTimeMonitor();
        var memE = PulseOptimize.getMemoryEngine();

        int fps       = fpsM  != null ? fpsM.getCurrentFps()        : -1;
        int low1p     = fpsM  != null ? fpsM.get1PercentLow()       : -1;
        int low01p    = fpsM  != null ? fpsM.get01PercentLow()      : -1;
        double ftMs   = ftM   != null ? ftM.getAverageFrameTimeMs() : -1;
        long usedMb   = memE  != null ? memE.getUsedMb()            : -1;
        String memP   = memE  != null ? memE.getPressureLabel()      : "UNKNOWN";

        String chunks   = performanceEngine != null ? performanceEngine.getChunkWorkload().name()   : "UNKNOWN";
        String entities = performanceEngine != null ? performanceEngine.getEntityWorkload().name()  : "UNKNOWN";
        String particles= performanceEngine != null ? performanceEngine.getParticleWorkload().name(): "UNKNOWN";

        return """
                You are a Minecraft Fabric performance advisor.
                Analyse the following sanitised client-side performance data and suggest
                PulseOptimize configuration changes to improve stability.

                Rules:
                - Base ALL suggestions on the data provided.
                - Do NOT invent hardware specifications.
                - Do NOT suggest server-side or gameplay changes.
                - ONLY suggest changes to PulseOptimize configuration options.
                - Distinguish observed data from estimated causes.
                - Format each recommendation as:
                  RECOMMENDATION: <short title>
                  REASON: <one sentence>
                  ACTION: <config field = value>

                Performance snapshot:
                Platform: Minecraft 1.21.1 / Fabric / Java 21
                FPS: %d
                1%% Low: %d
                0.1%% Low: %d
                Avg Frame Time: %.1f ms
                Memory Used: %d MB
                Memory Pressure: %s
                Chunk Workload: %s
                Entity Workload: %s
                Particle Workload: %s
                """.formatted(fps, low1p, low01p, ftMs, usedMb, memP, chunks, entities, particles);
    }

    /**
     * Sends a chat completion request to the configured provider.
     * Returns the assistant message text, or {@code null} on failure.
     */
    private String sendRequest(PulseConfig.AiProvider provider, String apiKey,
                                String model, String baseUrl, String userMessage)
            throws IOException, InterruptedException {
        return switch (provider) {
            case OPENROUTER -> sendOpenRouterRequest(apiKey, model, baseUrl, userMessage);
            case GEMINI     -> sendGeminiRequest(apiKey, model, userMessage);
        };
    }

    private String sendOpenRouterRequest(String apiKey, String model, String baseUrl,
                                          String userMessage)
            throws IOException, InterruptedException {
        String endpoint = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : OPENROUTER_ENDPOINT;

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        JsonArray messages = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", userMessage);
        messages.add(msg);
        body.add("messages", messages);
        body.addProperty("max_tokens", 512);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "https://github.com/ahmad/pulseoptimize")
                .header("X-Title", "PulseOptimize")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .timeout(TIMEOUT)
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            PulseOptimize.LOGGER.warn("[PulseOptimize] OpenRouter HTTP {}", resp.statusCode());
            return null;
        }

        // Parse OpenAI-compatible response
        JsonObject json = GSON.fromJson(resp.body(), JsonObject.class);
        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    private String sendGeminiRequest(String apiKey, String model, String userMessage)
            throws IOException, InterruptedException {
        String endpoint = GEMINI_ENDPOINT_TPL.formatted(model, apiKey);

        JsonObject part = new JsonObject();
        part.addProperty("text", userMessage);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject body = new JsonObject();
        body.add("contents", contents);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .timeout(TIMEOUT)
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            PulseOptimize.LOGGER.warn("[PulseOptimize] Gemini HTTP {}", resp.statusCode());
            return null;
        }

        JsonObject json = GSON.fromJson(resp.body(), JsonObject.class);
        return json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }

    /**
     * Parses structured recommendations out of the AI response text.
     * Lines that don't match the expected format are ignored gracefully.
     */
    private List<AiRecommendation> parseRecommendations(String raw) {
        List<AiRecommendation> list = new ArrayList<>();
        String title = null, reason = null, action = null;

        for (String line : raw.lines().map(String::trim).toList()) {
            if (line.startsWith("RECOMMENDATION:")) {
                // Flush previous entry
                if (title != null) list.add(new AiRecommendation(title, reason, action));
                title  = line.substring("RECOMMENDATION:".length()).trim();
                reason = null;
                action = null;
            } else if (line.startsWith("REASON:")) {
                reason = line.substring("REASON:".length()).trim();
            } else if (line.startsWith("ACTION:")) {
                action = line.substring("ACTION:".length()).trim();
            }
        }
        if (title != null) list.add(new AiRecommendation(title, reason, action));
        return list;
    }

    // ── Recommendation record ─────────────────────────────────────────────────

    public record AiRecommendation(String title, String reason, String action) {
        @Override
        public String toString() {
            return "RECOMMENDATION: " + title + "\nREASON: " + reason + "\nACTION: " + action;
        }
    }
}
