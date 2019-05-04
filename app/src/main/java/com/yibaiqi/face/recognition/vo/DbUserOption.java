package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/5/2.
 */
public class DbUserOption {
    private List<DbOption> add;
    private List<DbOption> update;
    private List<DbOption> delete;

    public List<DbOption> getAdd() {
        return add;
    }

    public void setAdd(List<DbOption> add) {
        this.add = add;
    }

    public List<DbOption> getUpdate() {
        return update;
    }

    public void setUpdate(List<DbOption> update) {
        this.update = update;
    }

    public List<DbOption> getDelete() {
        return delete;
    }

    public void setDelete(List<DbOption> delete) {
        this.delete = delete;
    }
}
