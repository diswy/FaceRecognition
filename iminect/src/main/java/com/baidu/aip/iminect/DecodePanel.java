package com.baidu.aip.iminect;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodePanel {

    private MediaCodec mCodec;

    public DecodePanel() {

    }

    public void initDecoder(Surface surface, int width, int height) {
        // init decoder
        try {
            mCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mCodec.configure(mediaFormat, surface, null, 0);
            mCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopDecoder() {
        // stop decoder
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
    }

    public void paint(ByteBuffer bufferImage, long timeStamp) {
        // queue inputbuffer
        if (bufferImage != null) {
            try {
                int inputBufferIndex = mCodec.dequeueInputBuffer(1000);
                if (inputBufferIndex >= 0) {
                    ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    inputBuffer.put(bufferImage);
                    mCodec.queueInputBuffer(inputBufferIndex, 0, bufferImage.capacity(), timeStamp, 0);
                }
            } catch (Exception e) {
                Log.e("gangzi", e.getMessage());
            }
        }

        // release outputbuffer
        try {
            BufferInfo info = new BufferInfo();
            int outputBufferIndex = mCodec.dequeueOutputBuffer(info, 1000);
            if (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, info.size != 0);
            }
        } catch (Exception e) {
            Log.e("gangzi", e.getMessage());
        }
    }
}
