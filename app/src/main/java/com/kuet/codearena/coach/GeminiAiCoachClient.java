package com.kuet.codearena.coach;

import android.util.Log;

import com.kuet.codearena.BuildConfig;
import com.kuet.codearena.coach.model.AnonymousUserStats;
import com.kuet.codearena.coach.model.GeminiRequest;
import com.kuet.codearena.coach.model.GeminiResponse;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.util.AppExecutors;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * AI coach client communicating with Google Gemini REST API using anonymous stats and strict 15s timeout fallbacks.
 */
public class GeminiAiCoachClient implements AiCoachClient {
    private static final String TAG = "GeminiAiCoachClient";
    private static final int TIMEOUT_SECONDS = 15;

    private final GeminiApi geminiApi;
    private final AppExecutors executors;

    public GeminiAiCoachClient() {
        this.executors = AppExecutors.getInstance();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Endpoints.GEMINI_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.geminiApi = retrofit.create(GeminiApi.class);
    }

    @Override
    public void getCoachingMessage(AnonymousUserStats stats, Callback callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;

        // If API key is not configured, fallback gracefully to offline rule-based message
        if (apiKey == null || apiKey.trim().isEmpty() || "null".equalsIgnoreCase(apiKey)) {
            String fallback = buildRuleBasedFallbackMessage(stats);
            executors.mainThread().execute(() -> {
                if (callback != null) callback.onResponse(fallback, false);
            });
            return;
        }

        executors.networkIO().execute(() -> {
            try {
                String weakTagsStr = stats.getWeakTags() != null ? String.join(", ", stats.getWeakTags()) : "general algorithms";
                String prompt = String.format(Locale.US,
                        "You are an expert competitive programming coach for CodeArena. "
                                + "Analyze these anonymous student performance metrics:\n"
                                + "- Practice Rating: %.0f\n"
                                + "- Current Active Streak: %d days\n"
                                + "- Solved Today: %d problems\n"
                                + "- Pending Upsolve Queue: %d items\n"
                                + "- Identified Weakness Tags: %s\n\n"
                                + "Provide a 2-sentence encouraging, highly tactical coaching summary explaining why "
                                + "reinforcing these weakness tags and climbing the 5-step ladder will boost their contest rating. "
                                + "Keep it under 60 words. Do not invent problem names or URLs.",
                        stats.getPracticeRating(),
                        stats.getStreakDays(),
                        stats.getSolvedTodayCount(),
                        stats.getPendingUpsolves(),
                        weakTagsStr
                );

                GeminiRequest request = new GeminiRequest(prompt);
                Response<GeminiResponse> response = geminiApi.generateContent(
                        Endpoints.GEMINI_MODEL,
                        apiKey,
                        request
                ).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String aiText = response.body().getFirstText();
                    if (aiText != null && !aiText.trim().isEmpty()) {
                        final String cleanText = aiText.trim();
                        executors.mainThread().execute(() -> {
                            if (callback != null) callback.onResponse(cleanText, true);
                        });
                        return;
                    }
                }

                // Fallback if empty response
                String fallback = buildRuleBasedFallbackMessage(stats);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onResponse(fallback, false);
                });

            } catch (Exception e) {
                Log.w(TAG, "Gemini AI generation failed, using rule-based engine: " + e.getMessage());
                String fallback = buildRuleBasedFallbackMessage(stats);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onResponse(fallback, false);
                });
            }
        });
    }

    private String buildRuleBasedFallbackMessage(AnonymousUserStats stats) {
        String weak = (stats.getWeakTags() != null && !stats.getWeakTags().isEmpty())
                ? stats.getWeakTags().get(0)
                : "dynamic programming";
        return String.format(Locale.US,
                "Focus on your '%s' weakness category today. Completing the 5-rung ladder anchored at rating %.0f "
                        + "will eliminate hesitation on standard transitions and safeguard your %d-day practice streak.",
                weak,
                stats.getPracticeRating(),
                stats.getStreakDays()
        );
    }
}

