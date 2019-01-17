package com.baidu.aip.iminect;

import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiFrameType;
import com.hjimi.api.iminect.ImiImageFrame;
import com.hjimi.api.iminect.Utils;

import java.nio.ByteBuffer;


public class SimpleViewer extends Thread {

    private boolean mShouldRun = false;

    private ImiFrameType mFrameType;
    private GLPanel mGlPanel;
    private DecodePanel mDecodePanel;
    private ImiDevice mDevice;
    private ImiFrameMode mCurrentMode;

    public SimpleViewer(ImiDevice device, ImiFrameType frameType) {
        mDevice = device;
        mFrameType = frameType;
    }

    public void setGLPanel(GLPanel gLPanel) {
        this.mGlPanel = gLPanel;
    }

    public void setDecodePanel(DecodePanel decodePanel) {
        this.mDecodePanel = decodePanel;
    }

    @Override
    public void run() {
        super.run();

        // open stream.
        mDevice.openStream(mFrameType);

        // get current framemode.
        mCurrentMode = mDevice.getCurrentFrameMode(mFrameType);

        // start read frame.
        while (mShouldRun) {
            ImiImageFrame nextFrame = mDevice.readNextFrame(mFrameType, 25);

            // frame maybe null, if null, continue.
            if (nextFrame == null) {
                continue;
            }

            switch (mFrameType) {
                case COLOR:
                    // draw color.
                    drawColor(nextFrame);
                    break;
                case DEPTH:
                    // draw depth.
                    drawDepth(nextFrame);
                    break;
                default:
                    break;
            }
        }
    }

    private void drawDepth(ImiImageFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();

        frameData = Utils.depth2RGB888(nextFrame, true, false);

        mGlPanel.paint(null, frameData, width, height);
    }

    private void drawColor(ImiImageFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();

        // draw color image.
        switch (mCurrentMode.getFormat()) {
            case IMI_PIXEL_FORMAT_IMAGE_H264:
                if (mDecodePanel != null) {
                    mDecodePanel.paint(frameData, nextFrame.getTimeStamp());
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_YUV420SP:
                frameData = Utils.yuv420sp2RGB(nextFrame);
                if (mGlPanel != null) {
                    mGlPanel.paint(null, frameData, width, height);
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_RGB24:
                if (mGlPanel != null) {
                    mGlPanel.paint(null, frameData, width, height);
                }
                break;
            default:
                break;
        }
    }

    public void onPause() {
        if (mGlPanel != null) {
            mGlPanel.onPause();
        }
    }

    public void onResume() {
        if (mGlPanel != null) {
            mGlPanel.onResume();
        }
    }

    public void onStart() {
        if (!mShouldRun) {
            mShouldRun = true;

            // start read thread
            this.start();
        }
    }

    public void onDestroy() {
        mShouldRun = false;

        // destroy stream.
        mDevice.closeStream(mFrameType);
    }
}
