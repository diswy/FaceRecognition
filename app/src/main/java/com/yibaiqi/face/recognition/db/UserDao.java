package com.yibaiqi.face.recognition.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yibaiqi.face.recognition.entity.User;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.LocalUser;
import com.yibaiqi.face.recognition.vo.MyRecord;

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

    @Query("SELECT * FROM DbOption")
    LiveData<List<DbOption>> observeAll();// 持续监听


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(List<MyRecord> list);

    // 本地用户表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<LocalUser> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(MyRecord myRecord);

    @Update
    void updateRecord(List<MyRecord> list);

    @Update
    void updateRecord(MyRecord myRecord);

    @Delete
    void deleteRecord(List<MyRecord> list);

    @Delete
    void deleteRecord(MyRecord dbOption);

    @Delete
    void deleteUsers(List<LocalUser> list);

    @Query("SELECT * FROM MyRecord")
    List<MyRecord> getRecordAll();// 只会执行一次

    @Query("SELECT * FROM LocalUser WHERE user_key is :user_key")
    LocalUser getUser(String user_key);

    @Query("SELECT * FROM MyRecord")
    LiveData<List<MyRecord>> observeRecordAll();// 只会执行一次
}
