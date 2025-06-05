package uz.alex2276564.leverlock.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

    @Getter
    public static class HttpResponse {
        private final int responseCode;
        private final JsonObject jsonBody;

        public HttpResponse(int responseCode, JsonObject jsonBody) {
            this.responseCode = responseCode;
            this.jsonBody = jsonBody;
        }
    }

    /**
     * Executes a GET request and returns an HttpResponse containing the response code and JSON body.
     *
     * @param urlString URL for the request.
     * @param userAgent User-Agent for the header (you can pass null if not needed).
     * @return HttpResponse containing the response code and JSON body.
     * @throws Exception if the request fails.
     */
    public HttpResponse getResponse(String urlString, String userAgent) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            int responseCode = connection.getResponseCode();
            JsonObject jsonBody = JsonParser.parseReader(reader).getAsJsonObject();

            return new HttpResponse(responseCode, jsonBody);
        } finally {
            connection.disconnect();
        }
    }
}
