package com.baidu.idl.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.idl.sample.R;

/**
 * Created by litonghui on 2018/11/21.
 */

public class DrawView extends View {

    private Paint mPaint;
    private RectF mRect;//矩形凹行大小
    private int mRoundRadius = 50;// 圆角大小
    private boolean mStyleFill = true;
    private int mColor = 0X343966;

    private int mWidthSize;
    private int mHeightSize;
    private int mStrokeWidth = 3;
    private float mAlpha = 1;


    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawViewAttr);
        mStyleFill = array.getBoolean(R.styleable.DrawViewAttr_drawStyleFill, true);
        mColor = array.getColor(R.styleable.DrawViewAttr_drawColor, 0X343966);
        mRoundRadius = (int) array.getDimension(R.styleable.DrawViewAttr_drawRoundRadius, 17);
        mStrokeWidth = (int) array.getDimension(R.styleable.DrawViewAttr_drawStrokeWidth, 1);
        mAlpha = array.getFloat(R.styleable.DrawViewAttr_drawAlpha, 1);
        array.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(mStyleFill ? Paint.Style.FILL : Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mColor);
        setAlpha(mAlpha);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRect = new RectF(mStrokeWidth, mStrokeWidth, mWidthSize - mStrokeWidth, mHeightSize - mStrokeWidth);
        canvas.drawRoundRect(mRect, mRoundRadius, mRoundRadius, mPaint);

    }
}
