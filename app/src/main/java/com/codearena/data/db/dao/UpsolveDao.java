package com.codearena.data.db.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.codearena.data.db.entity.UpsolveItemEntity;
import java.util.List;

@Dao
public interface UpsolveDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUpsolveItem(UpsolveItemEntity item);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUpsolveItems(List<UpsolveItemEntity> items);

    @Update
    void updateUpsolveItem(UpsolveItemEntity item);

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 ORDER BY nextReviewEpochMillis ASC")
    LiveData<List<UpsolveItemEntity>> getAllActiveUpsolvesLiveData();

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 ORDER BY nextReviewEpochMillis ASC")
    List<UpsolveItemEntity> getAllActiveUpsolves();

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 AND nextReviewEpochMillis <= :nowMillis ORDER BY nextReviewEpochMillis ASC")
    LiveData<List<UpsolveItemEntity>> getDueUpsolvesLiveData(long nowMillis);

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 AND nextReviewEpochMillis <= :nowMillis")
    List<UpsolveItemEntity> getDueUpsolves(long nowMillis);

    @Query("SELECT * FROM upsolve_items WHERE contestId = :contestId AND problemIndex = :index LIMIT 1")
    UpsolveItemEntity getUpsolveItem(long contestId, String index);

    @Query("SELECT COUNT(*) FROM upsolve_items WHERE isSolved = 1 AND nextReviewEpochMillis >= :startOfDayMillis")
    LiveData<Integer> getSolvedTodayCountLiveData(long startOfDayMillis);

    @Query("UPDATE upsolve_items SET isSolved = 1, nextReviewEpochMillis = :solvedAtMillis WHERE contestId = :contestId AND problemIndex = :index")
    void markAsSolved(long contestId, String index, long solvedAtMillis);

    @Query("DELETE FROM upsolve_items WHERE contestId = :contestId AND problemIndex = :index")
    void deleteItem(long contestId, String index);

    @Query("SELECT COUNT(*) FROM upsolve_items WHERE isSolved = 1")
    LiveData<Integer> getSolvedCountLiveData();

    @Query("SELECT COUNT(*) FROM upsolve_items WHERE isSolved = 1")
    int getSolvedCount();

    // Cursor for ContentProvider
    @Query("SELECT * FROM upsolve_items")
    Cursor getUpsolveCursor();
}

