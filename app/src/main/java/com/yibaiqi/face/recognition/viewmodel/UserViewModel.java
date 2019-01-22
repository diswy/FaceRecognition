package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.yibaiqi.face.recognition.repository.UserRepository;

import javax.inject.Inject;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    @Inject
    UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void say() {
        System.out.println("------>>>UserRepository" + userRepository);
    }
}
