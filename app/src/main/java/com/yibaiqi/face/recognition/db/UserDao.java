package com.yibaiqi.face.recognition.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yibaiqi.face.recognition.entity.User;
import com.yibaiqi.face.recognition.vo.DbOption;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<DbOption> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DbOption dbOption);

    @Update
    void update(List<DbOption> list);

    @Update
    void update(DbOption dbOption);

    @Delete
    void delete(List<DbOption> list);

    @Delete
    void delete(DbOption dbOption);

    @Query("SELECT * FROM DbOption")
    List<DbOption> getAll();// 只会执行一次

}
