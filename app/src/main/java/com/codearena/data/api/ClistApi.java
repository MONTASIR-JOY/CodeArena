package com.codearena.data.api;

import com.codearena.data.model.ClistResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Clist API declaration for upcoming contests across competitive programming platforms.
 */
public interface ClistApi {
    @GET("contest/")
    Call<ClistResponse> getUpcoming(
        @Header("Authorization") String authorization,
        @Query("resource") String resources,
        @Query("upcoming") boolean upcoming,
        @Query("order_by") String orderBy,
        @Query("limit") int limit
    );
}

