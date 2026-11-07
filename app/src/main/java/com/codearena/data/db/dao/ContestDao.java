package com.codearena.data.db.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.ContestEntity;
import java.util.List;

@Dao
public interface ContestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContests(List<ContestEntity> contests);

    @Query("SELECT * FROM contests WHERE phase = 'FINISHED' ORDER BY id DESC")
    LiveData<List<ContestEntity>> getFinishedContestsLiveData();

    @Query("SELECT * FROM contests WHERE phase = 'FINISHED' ORDER BY id DESC")
    List<ContestEntity> getFinishedContests();

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    LiveData<ContestEntity> getContestByIdLiveData(long contestId);

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    ContestEntity getContestById(long contestId);

    // SQLite Cursor query for ContentProvider inter-app sharing
    @Query("SELECT * FROM contests")
    Cursor getContestsCursor();

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    Cursor getContestByIdCursor(long contestId);
}

