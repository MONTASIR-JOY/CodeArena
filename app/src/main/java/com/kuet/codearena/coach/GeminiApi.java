package com.kuet.codearena.coach;

import com.kuet.codearena.coach.model.GeminiRequest;
import com.kuet.codearena.coach.model.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface communicating with the Google Gemini REST API.
 */
public interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiResponse> generateContent(
            @Path("model") String model,
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}
