package com.yibaiqi.face.recognition.viewmodel;

import com.yibaiqi.face.recognition.repository.SyncRepository;

import javax.inject.Inject;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
public class SyncViewModel {
    private final SyncRepository syncRepository;

    @Inject
    SyncViewModel(SyncRepository syncRepository) {
        this.syncRepository = syncRepository;
    }


}
