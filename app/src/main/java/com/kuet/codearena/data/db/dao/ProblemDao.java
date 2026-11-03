package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kuet.codearena.data.db.entity.ProblemEntity;

import java.util.List;

/**
 * Data Access Object managing queries for cached contest and problemset problems.
 */
@Dao
public interface ProblemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProblemEntity> problems);

    @Query("SELECT * FROM problems WHERE contestId = :contestId ORDER BY problemIndex ASC")
    LiveData<List<ProblemEntity>> getProblemsForContest(int contestId);

    @Query("SELECT * FROM problems WHERE contestId = :contestId ORDER BY problemIndex ASC")
    List<ProblemEntity> getProblemsForContestSync(int contestId);

    @Query("SELECT * FROM problems WHERE contestId = :contestId AND problemIndex = :index LIMIT 1")
    ProblemEntity getProblem(int contestId, String index);

    @Query("SELECT * FROM problems WHERE tags LIKE '%' || :tag || '%' AND rating BETWEEN :minRating AND :maxRating ORDER BY solvedCount DESC LIMIT 10")
    List<ProblemEntity> getCandidatesByTagAndRating(String tag, int minRating, int maxRating);

    @Query("SELECT COUNT(*) FROM problems")
    int getProblemCount();
}
