package com.alading.dream.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CacheDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Cache cache);


    @Query("select * from cache where `key`=:key")
    Cache getCache(String key);

    @Delete
    int delete(Cache cache);

    @Delete()
    int update(Cache cache);
}
