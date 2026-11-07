package com.codearena.data.api;

import com.codearena.data.model.CfBlogEntry;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.CfProblemsetResult;
import com.codearena.data.model.CfRatingChange;
import com.codearena.data.model.CfRecentAction;
import com.codearena.data.model.CfResponse;
import com.codearena.data.model.CfStandingsResult;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.model.CfUser;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Codeforces REST API declaration.
 */
public interface CodeforcesApi {

    @GET("contest.list")
    Call<CfResponse<List<CfContest>>> getContestList(@Query("gym") boolean gym);

    @GET("contest.standings")
    Call<CfResponse<CfStandingsResult>> getContestStandings(
        @Query("contestId") long contestId
    );

    @GET("user.status")
    Call<CfResponse<List<CfSubmission>>> getUserStatus(
        @Query("handle") String handle,
        @Query("from") int from,
        @Query("count") int count
    );

    @GET("user.info")
    Call<CfResponse<List<CfUser>>> getUserInfo(@Query("handles") String handles);

    @GET("user.rating")
    Call<CfResponse<List<CfRatingChange>>> getUserRating(@Query("handle") String handle);

    @GET("problemset.problems")
    Call<CfResponse<CfProblemsetResult>> getProblemset();

    @GET("problemset.problems")
    Call<CfResponse<CfProblemsetResult>> getProblemsetByTag(@Query("tags") String tags);

    @GET("recentActions")
    Call<CfResponse<List<CfRecentAction>>> getRecentActions(@Query("maxCount") int maxCount);

    @GET("blogEntry.view")
    Call<CfResponse<CfBlogEntry>> getBlogEntry(@Query("blogEntryId") long blogEntryId);

    @GET("user.blogEntries")
    Call<CfResponse<List<CfBlogEntry>>> getUserBlogEntries(@Query("handle") String handle);
}

