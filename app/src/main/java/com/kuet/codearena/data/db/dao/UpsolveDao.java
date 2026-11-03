package com.kuet.codearena.data.db.dao;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kuet.codearena.data.db.entity.UpsolveItemEntity;

import java.util.List;

/**
 * Data Access Object for managing upsolve queue items and ContentProvider queries.
 */
@Dao
public interface UpsolveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UpsolveItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UpsolveItemEntity> items);

    @Update
    void update(UpsolveItemEntity item);

    @Query("SELECT * FROM upsolve_items ORDER BY dueDateMs ASC")
    LiveData<List<UpsolveItemEntity>> getAllLive();

    @Query("SELECT * FROM upsolve_items ORDER BY dueDateMs ASC")
    List<UpsolveItemEntity> getAllSync();

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 ORDER BY dueDateMs ASC")
    LiveData<List<UpsolveItemEntity>> getPendingLive();

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 0 ORDER BY dueDateMs ASC")
    List<UpsolveItemEntity> getPendingSync();

    @Query("SELECT * FROM upsolve_items WHERE id = :id LIMIT 1")
    UpsolveItemEntity getItemById(long id);

    @Query("SELECT * FROM upsolve_items WHERE contestId = :contestId AND problemIndex = :index LIMIT 1")
    UpsolveItemEntity getItemByContestAndIndex(int contestId, String index);

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 1 AND solvedAtMs BETWEEN :startMs AND :endMs")
    List<UpsolveItemEntity> getSolvedBetween(long startMs, long endMs);

    @Query("SELECT * FROM upsolve_items WHERE isSolved = 1 AND solvedAtMs BETWEEN :startMs AND :endMs")
    LiveData<List<UpsolveItemEntity>> getSolvedBetweenLive(long startMs, long endMs);

    @Query("DELETE FROM upsolve_items WHERE id = :id")
    void deleteById(long id);

    // Methods supporting ContentProvider
    @Query("SELECT * FROM upsolve_items ORDER BY dueDateMs ASC")
    Cursor getUpsolveCursor();

    @Query("SELECT * FROM upsolve_items WHERE id = :id LIMIT 1")
    Cursor getUpsolveCursorById(long id);
}
