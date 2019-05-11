package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/5/11.
 */
public class Remote {
    private List<RemoteRecord> list;

    public Remote(List<RemoteRecord> list) {
        this.list = list;
    }

    public List<RemoteRecord> getList() {
        return list;
    }

    public void setList(List<RemoteRecord> list) {
        this.list = list;
    }
}
