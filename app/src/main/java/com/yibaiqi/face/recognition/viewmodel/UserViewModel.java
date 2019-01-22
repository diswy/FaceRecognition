package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yibaiqi.face.recognition.repository.UserRepository;
import com.yibaiqi.face.recognition.vo.Movie;
import com.yibaiqi.face.recognition.vo.Resource;

import javax.inject.Inject;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    @Inject
    UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void say() {
        System.out.println("------>>>UserRepository" + userRepository);
        userRepository.getMovie();
        userRepository.getMovie2();
    }

    public LiveData<Resource<Movie>> getMo2(){
        return userRepository.getMovie3();
    }
}
