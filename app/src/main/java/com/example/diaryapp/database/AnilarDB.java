package com.example.diaryapp.database;
import  android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.diaryapp.dao.AniDao;
import com.example.diaryapp.entities.Ani;


@Database(entities = Ani.class, version = 1, exportSchema = false)
public abstract class AnilarDB extends RoomDatabase {

    private static AnilarDB anilarDB;

    public static synchronized AnilarDB getDataBase(Context context){
        if(anilarDB == null){
            anilarDB = Room.databaseBuilder(
                    context,
                    AnilarDB.class,
                    "anilar_db"
            ).build();
        }
        return anilarDB;
    }

    public abstract AniDao aniDao();
}
