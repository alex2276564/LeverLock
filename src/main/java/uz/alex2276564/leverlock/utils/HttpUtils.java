package uz.alex2276564.leverlock.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Small wrapper around Java 11+ HttpClient for JSON-based HTTP calls.
 * <p>
 * Design goals:
 * - Centralize HTTP configuration (timeouts, version, etc.).
 * - Provide safe JSON parsing with sane defaults.
 * - Protect against DoS-style responses by limiting response body size.
 * <p>
 * This class does NOT do any semantic validation of JSON fields
 * (that is handled in SecurityUtils + call sites). It only concerns
 * itself with transport-level safety (timeouts / body size).
 */
public class HttpUtils {

    /**
     * Default per-request timeout (overall).
     */
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Connection establishment timeout.
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Default maximum response body size in bytes.
     */
    private static final int DEFAULT_MAX_BODY_BYTES = 256 * 1024; // 256 KiB

    private final HttpClient client;

    public HttpUtils() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    /**
     * Simplified response object: HTTP status code + parsed JSON body.
     * The JSON body is NEVER null; if parsing fails or body is empty,
     * an empty JSONObject is returned.
     */
    public record HttpResponse(int statusCode, JSONObject jsonBody) {
    }

    // =====================================================================
    // Public GET helpers
    // =====================================================================

    /**
     * GET JSON with default timeout and default max body size.
     */
    public HttpResponse getJson(String urlString, String userAgent)
            throws IOException, InterruptedException {
        return getJson(urlString, userAgent, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * GET JSON with explicit timeout and default max body size.
     */
    public HttpResponse getJson(String urlString, String userAgent, Duration timeout)
            throws IOException, InterruptedException {
        return getJson(urlString, userAgent, timeout, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * GET JSON with explicit timeout and explicit max body size.
     *
     * @param maxBodyBytes maximum number of bytes to read from the response body;
     *                     if <= 0, DEFAULT_MAX_BODY_BYTES is used.
     */
    public HttpResponse getJson(String urlString, String userAgent,
                                Duration timeout, int maxBodyBytes)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .timeout(timeout)
                .GET()
                .header("Accept", "application/json");

        if (userAgent != null) {
            builder.header("User-Agent", userAgent);
        }

        java.net.http.HttpResponse<InputStream> httpResponse = client.send(
                builder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofInputStream()
        );

        String body = readLimitedBody(httpResponse.body(), maxBodyBytes);
        JSONObject json = parseJsonQuietly(body);
        return new HttpResponse(httpResponse.statusCode(), json);
    }

    // =====================================================================
    // Public POST helpers
    // =====================================================================

    /**
     * POST JSON with default timeout and default max body size.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent)
            throws IOException, InterruptedException {
        return postJson(urlString, body, userAgent, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * POST JSON with explicit timeout and default max body size.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent, Duration timeout)
            throws IOException, InterruptedException {
        return postJson(urlString, body, userAgent, timeout, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * POST JSON with explicit timeout and explicit max body size.
     *
     * @param maxBodyBytes maximum number of bytes to read from the response body;
     *                     if <= 0, DEFAULT_MAX_BODY_BYTES is used.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent,
                                 Duration timeout, int maxBodyBytes)
            throws IOException, InterruptedException {

        String payload = (body == null) ? "" : body.toJSONString();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

        if (userAgent != null) {
            builder.header("User-Agent", userAgent);
        }

        java.net.http.HttpResponse<InputStream> httpResponse = client.send(
                builder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofInputStream()
        );

        String responseBody = readLimitedBody(httpResponse.body(), maxBodyBytes);
        JSONObject json = parseJsonQuietly(responseBody);
        return new HttpResponse(httpResponse.statusCode(), json);
    }

    // =====================================================================
    // Internal helpers
    // =====================================================================

    /**
     * Read the HTTP response body into a String, but stop and fail
     * if the size exceeds maxBodyBytes to avoid DoS via huge bodies.
     * <p>
     * If maxBodyBytes <= 0, DEFAULT_MAX_BODY_BYTES is used.
     */
    private String readLimitedBody(InputStream in, int maxBodyBytes) throws IOException {
        if (in == null) {
            return "";
        }

        int limit = (maxBodyBytes > 0) ? maxBodyBytes : DEFAULT_MAX_BODY_BYTES;

        try (InputStream is = in;
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;
            int total = 0;

            while ((read = is.read(buffer)) != -1) {
                total += read;
                if (total > limit) {
                    throw new IOException("HTTP response too large (>" + limit + " bytes)");
                }
                baos.write(buffer, 0, read);
            }

            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Parse a JSON string into a JSONObject.
     * <p>
     * - If body is null/empty, returns an empty JSONObject.
     * - If parsing fails, returns an empty JSONObject.
     * <p>
     * The caller can rely on jsonBody() never being null.
     */
    private JSONObject parseJsonQuietly(String body) {
        if (body == null || body.isEmpty()) {
            return new JSONObject();
        }
        try {
            JSONObject obj = JSON.parseObject(body);
            return (obj != null) ? obj : new JSONObject();
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }
}