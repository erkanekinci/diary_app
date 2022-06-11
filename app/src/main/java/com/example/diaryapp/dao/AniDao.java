package com.example.diaryapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.diaryapp.entities.Ani;
import java.util.List;


@Dao
public interface AniDao {

    @Query("SELECT * FROM anilar ORDER BY id DESC")
    List<Ani> getAllAni();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAni(Ani ani);

    @Delete
    void deleteAni(Ani ani);
}
