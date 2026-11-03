package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kuet.codearena.data.db.entity.ContestEntity;

import java.util.List;

/**
 * Data Access Object providing database queries for competitive programming contests.
 */
@Dao
public interface ContestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContestEntity> contests);

    @Query("SELECT * FROM contests WHERE platform = 'CODEFORCES' AND phase = 'FINISHED' ORDER BY startTimeSeconds DESC")
    LiveData<List<ContestEntity>> getPastCodeforcesContests();

    @Query("SELECT * FROM contests WHERE platform = 'CODEFORCES' AND phase = 'FINISHED' AND (name LIKE '%' || :query || '%' OR division LIKE '%' || :query || '%') ORDER BY startTimeSeconds DESC")
    LiveData<List<ContestEntity>> searchPastCodeforcesContests(String query);

    @Query("SELECT * FROM contests WHERE startTimeSeconds >= :nowSeconds ORDER BY startTimeSeconds ASC")
    LiveData<List<ContestEntity>> getUpcomingContests(long nowSeconds);

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    LiveData<ContestEntity> getContestByIdLive(int contestId);

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    ContestEntity getContestById(int contestId);

    @Query("SELECT COUNT(*) FROM contests")
    int getContestCount();

    @Query("DELETE FROM contests WHERE platform = :platform")
    void deleteByPlatform(String platform);
}
