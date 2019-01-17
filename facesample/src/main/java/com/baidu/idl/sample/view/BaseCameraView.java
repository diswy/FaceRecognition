package com.baidu.idl.sample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.idl.sample.callback.IFaceDetectCallBack;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.utils.ImageUtils;

public class BaseCameraView extends RelativeLayout implements IFaceDetectCallBack {

    protected ImageView faceFrameImg = null;
    private Context mContext;
    private Paint mPaint;
    private Paint mPaintHorn;
    private int mSizeFour;
    private int mSizeThirty;
    private int leftDisparity = 0;

    public BaseCameraView(Context context) {
        this(context, null, 0);
    }

    public BaseCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        // 绘制四个角时所需的长宽尺寸
        mSizeFour = ImageUtils.dip2px(mContext, 4);
        mSizeThirty = ImageUtils.dip2px(mContext, 30);
        // 绘制人脸框四个角的画笔
        mPaintHorn = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintHorn.setColor(Color.argb(255, 101, 227, 239));
        mPaintHorn.setStrokeWidth(ImageUtils.dip2px(mContext, 4));
        // 绘制人脸框四条边的画笔
        mPaint = new Paint();
        mPaint.setStrokeWidth(ImageUtils.dip2px(mContext, 1));
        mPaint.setTextSize(24);
        mPaint.setColor(Color.argb(155, 101, 227, 239));
        FaceSDKManager.getInstance().getFaceLiveness().setIFaceDetectCallBack(this);
    }

    public void initFaceFrame(Context context) {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        faceFrameImg = new ImageView(context);
        addView(faceFrameImg, lp);
        faceFrameImg.setVisibility(VISIBLE);
    }

    // 获取到摄像头预览区域的左边距
    public void leftDisparity(int leftDisparity) {
        this.leftDisparity = leftDisparity;
    }

    public void onFaceDetectCallback(final boolean isDetect, final int faceWidth, final int faceHeight,
                                     final int faceCenterX, final int faceCenterY, final int imgWidth,
                                     final int imgHeight) {
        post(new Runnable() {
            @Override
            public void run() {
                // 显示人脸框
                if (isDetect) {
                    // faceFrameImg.setVisibility(VISIBLE);
                    long timeStart = System.currentTimeMillis();
                    int viewWidht = getWidth()-leftDisparity*2;
                    int viewHeght = getHeight();
                    int frameX = (viewWidht * faceCenterX) / imgWidth;
                    int frameY = (viewHeght * faceCenterY) / imgHeight;
                    int frameW = (viewWidht * faceWidth) / imgWidth;
                    int frameH = (viewHeght * faceHeight) / imgHeight;

                    Bitmap canvasImg = Bitmap.createBitmap(viewWidht, viewHeght, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(canvasImg);

                    int topLeftX = frameX - frameW / 2;
                    int topLeftY = frameY - frameH / 2;
                    int topRightX = frameX + frameW / 2;
                    int topRightY = frameY - frameH / 2;
                    int bottomLeftX = frameX - frameW / 2;
                    int bottomLeftY = frameY + frameH / 2;
                    int bottomRightX = frameX + frameW / 2;
                    int bottomRightY = frameY + frameH / 2;

                    // 上
                    canvas.drawLine(topLeftX, topLeftY, topRightX, topRightY, mPaint);
                    // 下
                    canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY, mPaint);
                    // 左
                    canvas.drawLine(topLeftX, topLeftY, bottomLeftX, bottomLeftY, mPaint);
                    // 右
                    canvas.drawLine(topRightX, topRightY, bottomRightX, bottomRightY, mPaint);

                    // 绘制左上竖
                    canvas.drawRoundRect(topLeftX - mSizeFour, topLeftY - mSizeFour, topLeftX,
                            topLeftY + mSizeThirty, 10, 10, mPaintHorn);
                    // 绘制左上横
                    canvas.drawRoundRect(topLeftX - mSizeFour, topLeftY - mSizeFour,
                            topLeftX + mSizeThirty, topLeftY, 10, 10, mPaintHorn);
                    // 绘制左下竖
                    canvas.drawRoundRect(bottomLeftX - mSizeFour, bottomLeftY - mSizeThirty, bottomLeftX,
                            bottomLeftY + mSizeFour, 10, 10, mPaintHorn);
                    // 绘制左下横
                    canvas.drawRoundRect(bottomLeftX - mSizeFour, bottomLeftY, bottomLeftX + mSizeThirty,
                            bottomLeftY + mSizeFour, 10, 10, mPaintHorn);
                    // 绘制右上竖
                    canvas.drawRoundRect(topRightX, topRightY - mSizeFour, topRightX + mSizeFour,
                            topRightY + mSizeThirty, 10, 10, mPaintHorn);
                    // 绘制右上横
                    canvas.drawRoundRect(topRightX - mSizeThirty, topRightY - mSizeFour,
                            topRightX + mSizeFour, topRightY, 10, 10, mPaintHorn);
                    // 绘制右下竖
                    canvas.drawRoundRect(bottomRightX, bottomRightY - mSizeThirty, bottomRightX + mSizeFour,
                            bottomRightY + mSizeFour, 10, 10, mPaintHorn);
                    // 绘制右下横
                    canvas.drawRoundRect(bottomRightX - mSizeThirty, bottomRightY, bottomRightX + mSizeFour,
                            bottomRightY + mSizeFour, 10, 10, mPaintHorn);

                    faceFrameImg.setImageBitmap(canvasImg);
                    Log.i("yangrui", (System.currentTimeMillis() - timeStart) + "");
                } else {
                    faceFrameImg.setImageBitmap(null);
                }
            }
        });
    }
}
