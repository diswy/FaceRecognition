package com.yibaiqi.face.recognition.di;


import com.yibaiqi.face.recognition.di.ano.ActivityScope;
import com.yibaiqi.face.recognition.ui.ConfigActivity;
import com.yibaiqi.face.recognition.ui.SplashActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class)
public interface ActivityComponent {
    void inject(SplashActivity activity);
    void inject(ConfigActivity activity);
}
