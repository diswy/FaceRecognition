package com.orbbec.utils;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by zlh on 2015/8/4.
 */
public class FPSMeter {

    private long mPrevTS;
    private int mFPS;

    public void measure(String TAG, int minThreshold){

        if(GlobalDef.FPS_ON){
            long curTs = SystemClock.elapsedRealtime();
            long timeDiff = curTs - mPrevTS;
            if(timeDiff > 0){
                mFPS = (int)(1000 / (float)timeDiff);
                if(mFPS < minThreshold)
                {
                    Log.v(TAG, "FPS " + mFPS);
                }
            }
            mPrevTS = curTs;
        }
    }

    public int measure(){
        int fps = 0;
        long curTs = SystemClock.elapsedRealtime();
        long timeDiff = curTs - mPrevTS;
        if(timeDiff > 0){
            fps = (int)(1000 / (float)timeDiff);
        }
        mPrevTS = curTs;

        return fps;
    }
}
