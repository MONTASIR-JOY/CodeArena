package com.kuet.codearena.data.remote.api;

import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfContest;
import com.kuet.codearena.data.remote.model.CfProblemsResponse;
import com.kuet.codearena.data.remote.model.CfStandingsResponse;
import com.kuet.codearena.data.remote.model.CfSubmission;
import com.kuet.codearena.data.remote.model.CfUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface defining official Codeforces API endpoints.
 */
public interface CodeforcesApi {

    @GET(Endpoints.CF_CONTEST_LIST)
    Call<CfApiResponse<List<CfContest>>> getContests(@Query("gym") boolean gym);

    @GET(Endpoints.CF_CONTEST_STANDINGS)
    Call<CfApiResponse<CfStandingsResponse>> getContestStandings(
            @Query("contestId") int contestId,
            @Query("from") int from,
            @Query("count") int count,
            @Query("showUnofficial") boolean showUnofficial
    );

    @GET(Endpoints.CF_USER_STATUS)
    Call<CfApiResponse<List<CfSubmission>>> getUserStatus(
            @Query("handle") String handle,
            @Query("from") Integer from,
            @Query("count") Integer count
    );

    @GET(Endpoints.CF_USER_INFO)
    Call<CfApiResponse<List<CfUser>>> getUserInfo(
            @Query("handles") String handles
    );

    @GET(Endpoints.CF_PROBLEMSET_PROBLEMS)
    Call<CfApiResponse<CfProblemsResponse>> getProblemsetProblems(
            @Query("tags") String tags
    );
}
