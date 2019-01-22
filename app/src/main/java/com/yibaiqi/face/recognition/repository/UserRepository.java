package com.yibaiqi.face.recognition.repository;


import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.network.WebService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {
    private final UserDao userDao;
    private final WebService service;
    private final AppExecutors appExecutors;

    @Inject
    UserRepository(UserDao userDao, WebService service, AppExecutors appExecutors) {
        this.userDao = userDao;
        this.service = service;
        this.appExecutors = appExecutors;
    }

    
}
