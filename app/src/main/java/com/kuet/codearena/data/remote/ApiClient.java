package com.kuet.codearena.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuet.codearena.data.remote.api.CodeforcesApi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton factory creating configured Retrofit API instances with rate limiting and logging.
 */
public class ApiClient {
    private static OkHttpClient okHttpClient;
    private static CodeforcesApi codeforcesApi;
    private static Gson gson;

    public static synchronized Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setLenient()
                    .create();
        }
        return gson;
    }

    public static synchronized OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            // Rate limiter interceptor for Codeforces endpoints
            Interceptor cfRateLimiterInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    if (request.url().host().contains("codeforces.com")) {
                        RateLimiter.getCfInstance().acquire();
                    }
                    return chain.proceed(request);
                }
            };

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(cfRateLimiterInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }

    public static synchronized CodeforcesApi getCodeforcesApi() {
        if (codeforcesApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Endpoints.CODEFORCES_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
            codeforcesApi = retrofit.create(CodeforcesApi.class);
        }
        return codeforcesApi;
    }
}
