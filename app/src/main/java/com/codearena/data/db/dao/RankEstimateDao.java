package com.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.RankEstimateEntity;

@Dao
public interface RankEstimateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRankEstimate(RankEstimateEntity estimate);

    @Query("SELECT * FROM rank_estimates WHERE runId = :runId LIMIT 1")
    LiveData<RankEstimateEntity> getRankEstimateLiveData(long runId);

    @Query("SELECT * FROM rank_estimates WHERE runId = :runId LIMIT 1")
    RankEstimateEntity getRankEstimate(long runId);
}

