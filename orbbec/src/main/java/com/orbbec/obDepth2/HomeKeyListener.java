package com.orbbec.obDepth2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by linjx on 16-2-3.
 */
public class HomeKeyListener {
    static final String TAG = "HomeListener";

    public static final String FINISH = "finish";

    private Context mContext;
    private IntentFilter mHomeKeyFilter;
    private IntentFilter mFinishFilter;
    private OnHomePressedListener mListener;
    private InnerReceiver mReceiver;

    // 鍥炶皟鎺ュ彛
    public interface OnHomePressedListener {
        public void onHomePressed();

        public void onHomeLongPressed();
    }

    public HomeKeyListener(Context context) {
        mContext = context;
        mHomeKeyFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mFinishFilter = new IntentFilter();
    }

    /**
     * 璁剧疆鐩戝惉
     *
     * @param listener
     */
    public void setOnHomePressedListener(OnHomePressedListener listener) {
        mListener = listener;
        mReceiver = new InnerReceiver();
    }

    /**
     * 寮�鐩戝惉锛屾敞鍐屽箍鎾�     */
    public void startWatch() {
        if (mReceiver != null) {
            mContext.registerReceiver(mReceiver, mHomeKeyFilter);
        }
    }

    /**
     * 鍋滄鐩戝惉锛屾敞閿�箍鎾�     */
    public void stopWatch() {
        if (mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
            }

        }
    }

    class InnerReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    //    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (mListener != null) {
                        //TODO : see FutureBox
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            //    
                        	mListener.onHomePressed();
                        }/* else if (reason
                                .equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                                   mListener.onHomeLongPressed();
                        }*/
                    }
                }
            } else if (action.equals(FINISH)) {
                // 
            	mListener.onHomePressed();
            }
        }
    }
}