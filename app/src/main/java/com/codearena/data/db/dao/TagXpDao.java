package com.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.TagXpEntity;
import java.util.List;

@Dao
public interface TagXpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateTagXp(TagXpEntity tagXp);

    @Query("SELECT * FROM tag_xp ORDER BY xp DESC")
    LiveData<List<TagXpEntity>> getAllTagXpLiveData();

    @Query("SELECT * FROM tag_xp ORDER BY xp DESC")
    List<TagXpEntity> getAllTagXp();

    @Query("SELECT * FROM tag_xp WHERE tag = :tag LIMIT 1")
    TagXpEntity getTagXp(String tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TagXpEntity> tags);

    @Query("DELETE FROM tag_xp")
    void clear();
}

