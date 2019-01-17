/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.orbbec.view;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import com.orbbec.Native.DepthUtils;
import com.orbbec.utils.GlobalDef;

import org.openni.VideoFrameRef;
import org.openni.VideoStream;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xlj on 17-1-16.
 */

public class OpenGLView extends GLSurfaceView {

    protected int mSurfaceWidth = 0;
    protected int mSurfaceHeight = 0;

//    protected int mTextureWidth = 0;
//    protected int mTextureHeight = 0;
    protected ByteBuffer mTexture;
    protected int mTextureId = 0;

    private long mNativePtr = 0;

    private int mCurrFrameWidth = 0;
    private int mCurrFrameHeight = 0;

    private int mBaseColor = Color.WHITE;
    private String TAG = "OpenNIView";

    private int m_versionInt;

    public OpenGLView(Context context) {
        super(context);
        init();
    }

    public OpenGLView(Context context, AttributeSet attrs){
        super( context,  attrs);
        init();
    }

    private void init() {

        m_versionInt = Build.VERSION.SDK_INT;
        setRenderer(new Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig c) {
				/* Disable these capabilities. */
                final int gCapbilitiesToDisable[] = {
                        GLES10.GL_FOG,
                        GLES10.GL_LIGHTING,
                        GLES10.GL_CULL_FACE,
                        GLES10.GL_ALPHA_TEST,
                        GLES10.GL_BLEND,
                        GLES10.GL_COLOR_LOGIC_OP,
                        GLES10.GL_DITHER,
                        GLES10.GL_STENCIL_TEST,
                        GLES10.GL_DEPTH_TEST,
                        GLES10.GL_COLOR_MATERIAL,
                };

                for (int capability : gCapbilitiesToDisable)
                {
                    GLES10.glDisable(capability);
                }

                GLES10.glEnable(GLES10.GL_TEXTURE_2D);

                int ids[] = new int[1];
                GLES10.glGenTextures(1, ids, 0);
                mTextureId = ids[0];
                GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);

                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_LINEAR);
                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR);
                GLES10.glShadeModel(GLES10.GL_FLAT);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                synchronized (OpenGLView.this) {
                    mSurfaceWidth = w;
                    mSurfaceHeight = h;
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (OpenGLView.this) {
                    onDrawGL();
                }
            }
        });

        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void finalize() throws Throwable {
        if (mNativePtr != 0) {
            mNativePtr = 0;
        }
        super.finalize();
    }


    synchronized public void update(VideoStream stream, int type) {

        VideoFrameRef frame = stream.readFrame();
        mCurrFrameWidth = frame.getWidth();
        mCurrFrameHeight = frame.getHeight();

        if (mTexture == null) {
            // need to reallocate texture
            mTexture = ByteBuffer.allocateDirect(mCurrFrameWidth * mCurrFrameHeight * 4);
            Log.v(TAG, "mTexture: " + mTexture.limit());
        }
        ByteBuffer byteBuf = frame.getData();

        if (GlobalDef.TYPE_COLOR == type) {
            DepthUtils.RGB888TORGBA(byteBuf, mTexture, mCurrFrameWidth, mCurrFrameHeight,
                    frame.getStrideInBytes());
        } else if (GlobalDef.TYPE_DEPTH == type) {
            DepthUtils.ConvertTORGBA(byteBuf, mTexture, mCurrFrameWidth, mCurrFrameHeight,
                    frame.getStrideInBytes());
        }

        requestRender();
    }


    protected void onDrawGL() {
        if (mTexture == null || mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            return;
        }

        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
        int red = Color.red(mBaseColor);
        int green = Color.green(mBaseColor);
        int blue = Color.blue(mBaseColor);
        int alpha = Color.alpha(mBaseColor);
        GLES10.glColor4f(red/255.f, green/255.f, blue/255.f, alpha/255.f);

        GLES10.glEnable(GLES10.GL_TEXTURE_2D);

        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);
        int rect[] = {0, mCurrFrameHeight, mCurrFrameWidth, -mCurrFrameHeight};
        GLES11.glTexParameteriv(GLES10.GL_TEXTURE_2D, GLES11Ext.GL_TEXTURE_CROP_RECT_OES, rect, 0);

        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);

        GLES10.glTexImage2D(GLES10.GL_TEXTURE_2D, 0, GLES10.GL_RGBA, mCurrFrameWidth, mCurrFrameHeight, 0, GLES10.GL_RGBA,
                GLES10.GL_UNSIGNED_BYTE, mTexture);
        GLES11Ext.glDrawTexiOES(0, 0, 0, mSurfaceWidth, mSurfaceHeight);


        GLES10.glDisable(GLES10.GL_TEXTURE_2D);
    }
}
