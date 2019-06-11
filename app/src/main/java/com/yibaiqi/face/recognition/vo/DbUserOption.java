package com.yibaiqi.face.recognition.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by @author xiaofu on 2019/5/2.
 */
public class DbUserOption implements Serializable {
    @SerializedName(value = "add", alternate = {"change"})
    private List<DbOption> add;
    private List<DbOption> delete;

    public List<DbOption> getAdd() {
        return add;
    }

    public void setAdd(List<DbOption> add) {
        this.add = add;
    }

    public List<DbOption> getDelete() {
        return delete;
    }

    public void setDelete(List<DbOption> delete) {
        this.delete = delete;
    }
}
