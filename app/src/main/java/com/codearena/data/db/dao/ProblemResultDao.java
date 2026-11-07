package com.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.ProblemResultEntity;
import java.util.List;

@Dao
public interface ProblemResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertResults(List<ProblemResultEntity> results);

    @Query("SELECT * FROM problem_results WHERE runId = :runId ORDER BY problemIndex ASC")
    LiveData<List<ProblemResultEntity>> getResultsForRunLiveData(long runId);

    @Query("SELECT * FROM problem_results WHERE runId = :runId ORDER BY problemIndex ASC")
    List<ProblemResultEntity> getResultsForRun(long runId);

    @Query("SELECT * FROM problem_results")
    LiveData<List<ProblemResultEntity>> getAllResultsLiveData();

    @Query("SELECT * FROM problem_results")
    List<ProblemResultEntity> getAllResults();

    @Query("SELECT * FROM problem_results WHERE isSolved = 1")
    List<ProblemResultEntity> getAllSolvedResults();
}

