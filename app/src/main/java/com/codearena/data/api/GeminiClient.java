package com.codearena.data.api;

import com.codearena.BuildConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * GeminiClient: Client for Google Gemini generateContent REST endpoint.
 */
public class GeminiClient {
    private static final String MODEL = "gemini-2.0-flash";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface Callback {
        void onSuccess(String responseText);
        void onError(Exception error);
    }

    public static String generateSync(String prompt) throws Exception {
        String key = BuildConfig.GEMINI_API_KEY;
        if (key == null || key.trim().isEmpty()) {
            throw new Exception("AI coach needs an API key: add GEMINI_API_KEY=... to local.properties and rebuild.");
        }

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        RequestBody requestBody = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .header("x-goog-api-key", key)
                .post(requestBody)
                .build();

        try (Response response = http.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("AI service error (HTTP " + response.code() + ")");
            }
            String text = extractText(body);
            if (text == null || text.trim().isEmpty()) {
                throw new Exception("The AI returned an empty answer. Try again.");
            }
            return text.trim();
        }
    }

    public static void generateAsync(String prompt, Callback callback) {
        new Thread(() -> {
            try {
                String result = generateSync(prompt);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private static String extractText(String json) {
        try {
            JsonObject root = ApiClient.getGson().fromJson(json, JsonObject.class);
            if (root == null) return null;
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) return null;
            JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
            if (content == null) return null;
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null) return null;

            StringBuilder sb = new StringBuilder();
            for (JsonElement part : parts) {
                if (part.isJsonObject() && part.getAsJsonObject().has("text")) {
                    sb.append(part.getAsJsonObject().get("text").getAsString());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

