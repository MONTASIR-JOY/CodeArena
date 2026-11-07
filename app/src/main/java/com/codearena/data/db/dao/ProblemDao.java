package com.codearena.data.db.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.ProblemEntity;
import java.util.List;

@Dao
public interface ProblemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProblems(List<ProblemEntity> problems);

    @Query("SELECT * FROM problems WHERE contestId = :contestId ORDER BY `index` ASC")
    LiveData<List<ProblemEntity>> getProblemsForContestLiveData(long contestId);

    @Query("SELECT * FROM problems WHERE contestId = :contestId ORDER BY `index` ASC")
    List<ProblemEntity> getProblemsForContest(long contestId);

    @Query("SELECT * FROM problems WHERE contestId = :contestId AND `index` = :index LIMIT 1")
    ProblemEntity getProblem(long contestId, String index);

    // Cursor for ContentProvider
    @Query("SELECT * FROM problems")
    Cursor getProblemsCursor();
}

