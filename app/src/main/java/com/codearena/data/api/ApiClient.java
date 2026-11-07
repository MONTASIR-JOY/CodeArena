package com.codearena.data.api;

import com.codearena.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient: Central networking factory configuring Retrofit, OkHttp, and Gson.
 */
public class ApiClient {
    private static final String CF_BASE_URL = "https://codeforces.com/api/";
    private static final String CLIST_BASE_URL = "https://clist.by/api/v4/";

    private static Gson gsonInstance;
    private static OkHttpClient cfOkHttpClient;
    private static CodeforcesApi codeforcesApiInstance;
    private static ClistApi clistApiInstance;

    public static synchronized Gson getGson() {
        if (gsonInstance == null) {
            gsonInstance = new GsonBuilder()
                    .setLenient()
                    .create();
        }
        return gsonInstance;
    }

    private static synchronized OkHttpClient getCfOkHttpClient() {
        if (cfOkHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BASIC :
                    HttpLoggingInterceptor.Level.NONE);

            cfOkHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new RateLimitInterceptor(2000L))
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        return cfOkHttpClient;
    }

    public static synchronized CodeforcesApi getCodeforcesApi() {
        if (codeforcesApiInstance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CF_BASE_URL)
                    .client(getCfOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
            codeforcesApiInstance = retrofit.create(CodeforcesApi.class);
        }
        return codeforcesApiInstance;
    }

    public static synchronized ClistApi getClistApi() {
        if (clistApiInstance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CLIST_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
            clistApiInstance = retrofit.create(ClistApi.class);
        }
        return clistApiInstance;
    }

    private static OkHttpClient generalHttpClient;

    public static synchronized OkHttpClient getGeneralHttpClient() {
        if (generalHttpClient == null) {
            generalHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .build();
        }
        return generalHttpClient;
    }
}

