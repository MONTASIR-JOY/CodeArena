package com.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.NoteEntity;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveNote(NoteEntity note);

    @Query("SELECT * FROM problem_notes WHERE contestId = :contestId AND problemIndex = :index LIMIT 1")
    LiveData<NoteEntity> getNoteLiveData(long contestId, String index);

    @Query("SELECT * FROM problem_notes WHERE contestId = :contestId AND problemIndex = :index LIMIT 1")
    NoteEntity getNote(long contestId, String index);

    @Query("SELECT * FROM problem_notes WHERE contestId = :contestId")
    LiveData<List<NoteEntity>> getNotesForContestLiveData(long contestId);

    @Query("DELETE FROM problem_notes WHERE contestId = :contestId AND problemIndex = :index")
    void deleteNote(long contestId, String index);
}

