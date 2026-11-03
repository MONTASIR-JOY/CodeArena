package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.RunProblemEntity;

import java.util.List;

/**
 * Data Access Object for persisting and retrieving virtual contest runs and problem outcomes.
 */
@Dao
public interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRun(RunEntity run);

    @Update
    void updateRun(RunEntity run);

    @Query("SELECT * FROM runs WHERE runId = :runId LIMIT 1")
    RunEntity getRunById(long runId);

    @Query("SELECT * FROM runs WHERE runId = :runId LIMIT 1")
    LiveData<RunEntity> getRunByIdLive(long runId);

    @Query("SELECT * FROM runs ORDER BY startTimeMs DESC")
    LiveData<List<RunEntity>> getAllRunsLive();

    @Query("SELECT * FROM runs WHERE status = 'FINISHED' ORDER BY finishTimeMs DESC LIMIT :limit")
    List<RunEntity> getLastFinishedRuns(int limit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRunProblems(List<RunProblemEntity> runProblems);

    @Update
    void updateRunProblem(RunProblemEntity runProblem);

    @Query("SELECT * FROM run_problems WHERE runId = :runId ORDER BY problemIndex ASC")
    LiveData<List<RunProblemEntity>> getRunProblemsLive(long runId);

    @Query("SELECT * FROM run_problems WHERE runId = :runId ORDER BY problemIndex ASC")
    List<RunProblemEntity> getRunProblemsSync(long runId);
}
