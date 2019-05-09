package com.yibaiqi.face.recognition.di;


import com.yibaiqi.face.recognition.di.ano.ActivityScope;
import com.yibaiqi.face.recognition.ui.core.CMainActivity;
import com.yibaiqi.face.recognition.ui.core.CameraSettingsActivity;
import com.yibaiqi.face.recognition.ui.core.SplashActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class)
public interface ActivityComponent {
    void inject(SplashActivity activity);
    void inject(CameraSettingsActivity activity);
}
