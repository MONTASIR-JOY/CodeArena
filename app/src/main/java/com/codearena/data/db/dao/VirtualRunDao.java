package com.codearena.data.db.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.codearena.data.db.entity.VirtualRunEntity;
import java.util.List;

@Dao
public interface VirtualRunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRun(VirtualRunEntity run);

    @Update
    void updateRun(VirtualRunEntity run);

    @Query("SELECT * FROM virtual_runs WHERE completed = 1 ORDER BY startTimeEpochMillis DESC")
    LiveData<List<VirtualRunEntity>> getAllCompletedRunsLiveData();

    @Query("SELECT * FROM virtual_runs WHERE completed = 1 ORDER BY startTimeEpochMillis DESC")
    List<VirtualRunEntity> getAllCompletedRuns();

    @Query("SELECT * FROM virtual_runs WHERE runId = :runId LIMIT 1")
    LiveData<VirtualRunEntity> getRunByIdLiveData(long runId);

    @Query("SELECT * FROM virtual_runs WHERE runId = :runId LIMIT 1")
    VirtualRunEntity getRunById(long runId);

    @Query("SELECT * FROM virtual_runs ORDER BY runId DESC LIMIT 1")
    VirtualRunEntity getLatestRun();

    // Cursor for ContentProvider
    @Query("SELECT * FROM virtual_runs")
    Cursor getVirtualRunsCursor();
}

