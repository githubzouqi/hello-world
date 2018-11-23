package com.example.pc2.carmapproject.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.drm.DrmStore;
import android.support.annotation.Nullable;

import com.example.pc2.carmapproject.utils.LogUtil;

public class RcsTripService extends IntentService {

    public RcsTripService() {
        super("RcsTripService");
    }

    @Override
    public void onCreate() {
        LogUtil.e("RcsTripService", "onCreate");
        super.onCreate();
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
//        String action = intent.getAction();
//        LogUtil.e("RcsTripService", "onStart, action = " + action);

        super.onStart(intent, startId);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        String action = intent.getAction();
//        LogUtil.e("RcsTripService", "onHandleIntent, action = " + action);
    }

    @Override
    public void onDestroy() {
        LogUtil.e("RcsTripService", "onDestroy");
        super.onDestroy();
    }
}
