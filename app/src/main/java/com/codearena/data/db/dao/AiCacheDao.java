package com.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.codearena.data.db.entity.AiCacheEntity;

@Dao
public interface AiCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCache(AiCacheEntity cache);

    @Query("SELECT * FROM ai_cache WHERE cacheKey = :key LIMIT 1")
    LiveData<AiCacheEntity> getCacheLiveData(String key);

    @Query("SELECT * FROM ai_cache WHERE cacheKey = :key LIMIT 1")
    AiCacheEntity getCache(String key);

    @Query("DELETE FROM ai_cache WHERE cacheKey = :key")
    void deleteCache(String key);
}

