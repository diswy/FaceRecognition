package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class DbClassOption {
    private List<DbClassOptionContent> change;
    private List<DbClassOptionContent> delete;

    public List<DbClassOptionContent> getChange() {
        return change;
    }

    public void setChange(List<DbClassOptionContent> change) {
        this.change = change;
    }

    public List<DbClassOptionContent> getDelete() {
        return delete;
    }

    public void setDelete(List<DbClassOptionContent> delete) {
        this.delete = delete;
    }
}
