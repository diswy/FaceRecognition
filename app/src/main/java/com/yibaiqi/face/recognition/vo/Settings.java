package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class Settings {
    private boolean delete;
    private List<SettingContent> change;

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public List<SettingContent> getChange() {
        return change;
    }

    public void setChange(List<SettingContent> change) {
        this.change = change;
    }
}
