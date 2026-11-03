package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kuet.codearena.data.db.entity.SolveEntity;

import java.util.List;

/**
 * Data Access Object for cross-platform solved problem records.
 */
@Dao
public interface SolveDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(SolveEntity solve);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<SolveEntity> solves);

    @Query("SELECT * FROM solves ORDER BY solvedAt DESC")
    LiveData<List<SolveEntity>> getAllSolvesLive();

    @Query("SELECT * FROM solves ORDER BY solvedAt DESC")
    List<SolveEntity> getAllSolvesSync();

    @Query("SELECT * FROM solves WHERE solvedAt BETWEEN :startMs AND :endMs ORDER BY solvedAt DESC")
    LiveData<List<SolveEntity>> getSolvesBetweenLive(long startMs, long endMs);

    @Query("SELECT * FROM solves WHERE solvedAt BETWEEN :startMs AND :endMs ORDER BY solvedAt DESC")
    List<SolveEntity> getSolvesBetweenSync(long startMs, long endMs);

    @Query("SELECT COUNT(*) FROM solves WHERE platform = :platform AND problemId = :problemId")
    int countMatching(String platform, String problemId);

    @Query("SELECT COUNT(*) FROM solves")
    int getTotalSolveCount();

    @Query("SELECT * FROM solves WHERE platform = :platform ORDER BY solvedAt DESC")
    List<SolveEntity> getSolvesByPlatform(String platform);
}
