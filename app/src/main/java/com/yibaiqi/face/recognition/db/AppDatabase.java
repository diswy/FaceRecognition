package com.yibaiqi.face.recognition.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.MyRecord;

@Database(entities = {DbOption.class, MyRecord.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
