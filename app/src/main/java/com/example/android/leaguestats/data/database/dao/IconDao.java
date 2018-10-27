package com.example.android.leaguestats.data.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.leaguestats.data.database.entity.IconEntry;

import java.util.List;

@Dao
public interface IconDao {

    @Query("SELECT * FROM icon")
    LiveData<List<IconEntry>> loadAllIcons();

    @Query("SELECT * FROM icon WHERE id = :id")
    LiveData<IconEntry> loadIconById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIcon(IconEntry iconEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(IconEntry... iconEntries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIcon(IconEntry iconEntry);

    @Query("DELETE FROM icon")
    void deleteIcons();

    @Query("SELECT COUNT(id) FROM icon")
    int countAllIcons();
}
