package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class Leaves {
    private List<DbLeaveOptionContent> change;
    private List<DbLeaveOptionContent> delete;

    public List<DbLeaveOptionContent> getChange() {
        return change;
    }

    public void setChange(List<DbLeaveOptionContent> change) {
        this.change = change;
    }

    public List<DbLeaveOptionContent> getDelete() {
        return delete;
    }

    public void setDelete(List<DbLeaveOptionContent> delete) {
        this.delete = delete;
    }
}
