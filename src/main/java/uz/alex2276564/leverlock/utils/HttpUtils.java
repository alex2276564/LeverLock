package uz.alex2276564.leverlock.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Small wrapper around Java 11+ HttpClient for JSON-based HTTP calls.
 * <p>
 * Design goals:
 * - Centralize HTTP configuration (timeouts, version, redirect policy, etc.).
 * - Never throw checked exceptions to callers: every failure (connection,
 * timeout, oversized body, malformed JSON) is reported as a typed
 * {@link HttpErrorType} inside {@link HttpResponse}.
 * - Protect against DoS-style responses by limiting response body size.
 * - Do not follow redirects implicitly; callers must specify the final URL.
 * <p>
 * This class does NOT do any semantic validation of JSON fields
 * (that is handled in SecurityUtils + call sites). It only concerns
 * itself with transport-level safety (timeouts / body size limits / redirects).
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
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    // =====================================================================
    // Result types
    // =====================================================================

    /**
     * Transport/parsing-level error classification.
     * <p>
     * NONE means the HTTP request/response cycle completed and the body
     * (if any) was parsed as JSON without issues. This does NOT reflect
     * HTTP status code semantics (404 / 500 / etc.) — callers are still
     * responsible for checking {@link HttpResponse#statusCode()} for
     * API-level errors.
     */
    public enum HttpErrorType {
        NONE,
        CONNECTION_FAILED,
        TIMEOUT,
        INTERRUPTED,
        RESPONSE_TOO_LARGE,
        INVALID_JSON,
        UNKNOWN
    }

    /**
     * Result of an HTTP call.
     * <p>
     * - statusCode: -1 if the request never got a response (transport failure).
     * - jsonBody: never null; empty JSONObject if there was no body, the body
     * wasn't valid JSON, or the request failed before a response arrived.
     * - errorType: NONE on success, otherwise describes what went wrong.
     * - errorMessage: optional low-level detail (safe to log as-is).
     */
    public record HttpResponse(
            int statusCode,
            @NotNull JSONObject jsonBody,
            @NotNull HttpErrorType errorType,
            @Nullable String errorMessage
    ) {
        public boolean hasError() {
            return errorType != HttpErrorType.NONE;
        }

        /**
         * True only if the transport succeeded AND the status code is 2xx.
         */
        public boolean isOk() {
            return !hasError() && statusCode >= 200 && statusCode < 300;
        }

        /**
         * Human-readable, log-friendly description of the error (or "no error").
         */
        public String describeError() {
            String base = switch (errorType) {
                case NONE -> "no error";
                case TIMEOUT -> "request timed out";
                case CONNECTION_FAILED -> "connection failed";
                case RESPONSE_TOO_LARGE -> "response too large";
                case INVALID_JSON -> "response body is not valid JSON";
                case INTERRUPTED -> "request was interrupted";
                case UNKNOWN -> "unknown error";
            };
            return (errorMessage != null && !errorMessage.isBlank())
                    ? base + " (" + errorMessage + ")"
                    : base;
        }

        private static HttpResponse ok(int statusCode, JSONObject jsonBody) {
            return new HttpResponse(statusCode, jsonBody, HttpErrorType.NONE, null);
        }

        private static HttpResponse parseError(int statusCode, String message) {
            return new HttpResponse(statusCode, new JSONObject(), HttpErrorType.INVALID_JSON, message);
        }

        private static HttpResponse failure(HttpErrorType errorType, String message) {
            return new HttpResponse(-1, new JSONObject(), errorType, message);
        }
    }

    // =====================================================================
    // Public GET helpers
    // =====================================================================

    /**
     * GET JSON with default timeout and default max body size.
     */
    public HttpResponse getJson(String urlString, String userAgent) {
        return getJson(urlString, userAgent, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * GET JSON with explicit timeout and default max body size.
     */
    public HttpResponse getJson(String urlString, String userAgent, Duration timeout) {
        return getJson(urlString, userAgent, timeout, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * GET JSON with explicit timeout and explicit max body size.
     *
     * @param maxBodyBytes maximum number of bytes to read from the response body;
     *                     if <= 0, DEFAULT_MAX_BODY_BYTES is used.
     */
    public HttpResponse getJson(String urlString, String userAgent, Duration timeout, int maxBodyBytes) {
        HttpRequest.Builder builder;
        try {
            builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(timeout)
                    .GET()
                    .header("Accept", "application/json");
        } catch (IllegalArgumentException e) {
            return HttpResponse.failure(HttpErrorType.UNKNOWN, "Invalid URL: " + e.getMessage());
        }

        if (userAgent != null) {
            builder.header("User-Agent", userAgent);
        }

        return execute(builder.build(), maxBodyBytes);
    }

    // =====================================================================
    // Public POST helpers
    // =====================================================================

    /**
     * POST JSON with default timeout and default max body size.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent) {
        return postJson(urlString, body, userAgent, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * POST JSON with explicit timeout and default max body size.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent, Duration timeout) {
        return postJson(urlString, body, userAgent, timeout, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * POST JSON with explicit timeout and explicit max body size.
     *
     * @param maxBodyBytes maximum number of bytes to read from the response body;
     *                     if <= 0, DEFAULT_MAX_BODY_BYTES is used.
     */
    public HttpResponse postJson(String urlString, JSONObject body, String userAgent,
                                 Duration timeout, int maxBodyBytes) {
        String payload = (body == null) ? "" : body.toJSONString();

        HttpRequest.Builder builder;
        try {
            builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return HttpResponse.failure(HttpErrorType.UNKNOWN, "Invalid URL: " + e.getMessage());
        }

        if (userAgent != null) {
            builder.header("User-Agent", userAgent);
        }

        return execute(builder.build(), maxBodyBytes);
    }

    // =====================================================================
    // Internal execution
    // =====================================================================

    private HttpResponse execute(HttpRequest request, int maxBodyBytes) {
        try {
            java.net.http.HttpResponse<InputStream> httpResponse = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofInputStream()
            );

            String body;
            try {
                body = readLimitedBody(httpResponse.body(), maxBodyBytes);
            } catch (ResponseTooLargeException e) {
                return HttpResponse.failure(HttpErrorType.RESPONSE_TOO_LARGE, e.getMessage());
            }

            if (body.isEmpty()) {
                return HttpResponse.ok(httpResponse.statusCode(), new JSONObject());
            }

            try {
                JSONObject json = JSON.parseObject(body);
                if (json == null) {
                    json = new JSONObject();
                }
                return HttpResponse.ok(httpResponse.statusCode(), json);
            } catch (Exception e) {
                // Body WAS received (and we have a real status code),
                // it just isn't valid JSON — tell the caller instead of hiding it.
                return HttpResponse.parseError(httpResponse.statusCode(), e.getMessage());
            }

        } catch (HttpTimeoutException e) { // covers HttpConnectTimeoutException as well
            return HttpResponse.failure(HttpErrorType.TIMEOUT, e.getMessage());
        } catch (IOException e) {
            return HttpResponse.failure(HttpErrorType.CONNECTION_FAILED, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return HttpResponse.failure(HttpErrorType.INTERRUPTED, e.getMessage());
        } catch (Exception e) {
            return HttpResponse.failure(HttpErrorType.UNKNOWN, e.getMessage());
        }
    }

    /**
     * Read the HTTP response body into a String, but stop and fail
     * if the size exceeds maxBodyBytes to avoid DoS via huge bodies.
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
                    throw new ResponseTooLargeException("HTTP response too large (>" + limit + " bytes)");
                }
                baos.write(buffer, 0, read);
            }

            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class ResponseTooLargeException extends IOException {
        ResponseTooLargeException(String message) {
            super(message);
        }
    }
}