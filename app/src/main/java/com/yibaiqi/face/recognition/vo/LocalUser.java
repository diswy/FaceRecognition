package com.yibaiqi.face.recognition.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by @author xiaofu on 2019/6/11.
 */
@Entity
public class LocalUser {
    @PrimaryKey
    @NonNull
    private String user_key;
    @ColumnInfo(name = "real_name")
    private String real_name;

    public LocalUser(@NonNull String user_key, String real_name) {
        this.user_key = user_key;
        this.real_name = real_name;
    }

    @NonNull
    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(@NonNull String user_key) {
        this.user_key = user_key;
    }

    public String getReal_name() {
        return real_name;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }
}
