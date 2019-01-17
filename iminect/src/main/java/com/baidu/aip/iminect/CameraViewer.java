package com.baidu.aip.iminect;

import com.hjimi.api.iminect.ImiCamera;
import com.hjimi.api.iminect.ImiCameraFrame;
import com.hjimi.api.iminect.ImiCameraFrameMode;
import com.hjimi.api.iminect.ImiCameraPixelFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CameraViewer extends Thread {

    private boolean mShouldRun = false;
    private boolean mPauseRun = true;

    private int mCount = 0;
    private int mFps = 0;

    private GLPanel mGLPanel;
    private ImiCamera mCamera;
    private Lock mStreamHandleLock = null;

    private ImiCameraFrameMode mFrameMode;

    public CameraViewer(ImiCamera camera) {
        mCamera = camera;
        mFrameMode = new ImiCameraFrameMode(ImiCameraPixelFormat.IMI_CAMERA_PIXEL_FORMAT_RGB888, 640, 480, 30);
        mStreamHandleLock = new ReentrantLock();
    }

    public void setGLPanel(GLPanel glPanel) {
        mPauseRun = true;
        this.mGLPanel = glPanel;
        mPauseRun = false;
    }

    public void onViewer() {
        if (mCamera != null) {
            mCamera.startPreview(mFrameMode);
        }
        mPauseRun = false;
    }

    public void offViewer() {
        mPauseRun = true;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCount = 0;
            mFps = 0;
        }
    }

    public boolean isViewerOn() {
        return !mPauseRun;
    }

    public int getFps() {
        return mFps;
    }

    public boolean isRun() {
        return mShouldRun;
    }

    @Override
    public void run() {
        super.run();
        long startTime = 0;
        long endTime = 0;

        // start read frame.
        while (mShouldRun) {

            if (!mPauseRun) {
                // stream handle lock
                // mStreamHandleLock.lock();

                ImiCameraFrame nextFrame = null;
                if (mCamera != null) {
                    nextFrame = mCamera.readNextFrame(40);
                }

                // mStreamHandleLock.unlock();

                // frame maybe null, if null, continue.
                if (nextFrame == null) {
                    continue;
                }

                mCount++;
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }

                endTime = System.currentTimeMillis();
                if (endTime - startTime >= 1000) {
                    mFps = mCount;
                    mCount = 0;
                    startTime = endTime;
                }
                // draw color.
                drawColor(nextFrame);
            } else {
                Thread.yield();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void drawColor(ImiCameraFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();

        if (mGLPanel != null) {
            mGLPanel.paint(null, frameData, width, height);
        }

    }


    public void onStart() {
        if (!mShouldRun) {
            mShouldRun = true;
            start();
        }
    }

    public void onPause() {
        if (mGLPanel != null) {
            mGLPanel.onPause();
        }
    }

    public void onResume() {
        if (mGLPanel != null) {
            mGLPanel.onResume();
        }
    }


    public void onDestroy() {
        mShouldRun = false;
    }

    public void setFrameMode(ImiCameraFrameMode frameMode) {
        mFrameMode = frameMode;
    }

    public ImiCameraFrameMode getCurrentMode() {
        return mFrameMode;
    }


    public int getStreamWidth() {
        if (mFrameMode != null) {
            return mFrameMode.getWidth();
        }
        return 0;
    }

    public int getStreamHeight() {
        if (mFrameMode != null) {
            return mFrameMode.getHeight();
        }
        return 0;
    }
}
