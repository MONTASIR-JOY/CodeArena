package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kuet.codearena.data.db.entity.DailyStatEntity;

import java.util.List;

/**
 * Data Access Object for accessing and mutating aggregated daily problem-solving metrics.
 */
@Dao
public interface DailyStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DailyStatEntity dailyStat);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateAll(List<DailyStatEntity> dailyStats);

    @Query("SELECT * FROM daily_stats WHERE dateStr = :dateStr LIMIT 1")
    DailyStatEntity getStatForDate(String dateStr);

    @Query("SELECT * FROM daily_stats WHERE dateStr = :dateStr LIMIT 1")
    LiveData<DailyStatEntity> getStatForDateLive(String dateStr);

    @Query("SELECT * FROM daily_stats WHERE dateStr BETWEEN :startDateStr AND :endDateStr ORDER BY dateStr ASC")
    List<DailyStatEntity> getStatsBetween(String startDateStr, String endDateStr);

    @Query("SELECT * FROM daily_stats WHERE dateStr BETWEEN :startDateStr AND :endDateStr ORDER BY dateStr ASC")
    LiveData<List<DailyStatEntity>> getStatsBetweenLive(String startDateStr, String endDateStr);

    @Query("SELECT * FROM daily_stats ORDER BY dateStr DESC")
    LiveData<List<DailyStatEntity>> getAllStatsLive();

    @Query("SELECT * FROM daily_stats ORDER BY dateStr DESC")
    List<DailyStatEntity> getAllStatsSync();
}
