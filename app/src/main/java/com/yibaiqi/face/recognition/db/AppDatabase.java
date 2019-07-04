package com.yibaiqi.face.recognition.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.yibaiqi.face.recognition.vo.DbClassOptionContent;
import com.yibaiqi.face.recognition.vo.DbLeaveOptionContent;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.LocalUser;
import com.yibaiqi.face.recognition.vo.MyRecord;
import com.yibaiqi.face.recognition.vo.SettingContent;

@Database(entities = {DbOption.class, MyRecord.class, LocalUser.class,
        DbClassOptionContent.class, DbLeaveOptionContent.class, SettingContent.class,
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();


    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE LocalUser (user_key TEXT, real_name TEXT, PRIMARY KEY(user_key))"
            );
        }
    };
}
