package com.app.loadmore;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PageIndicator extends View {

    private final static int DEFAULT_WIDTH = 90;
    private final static int DEFAULT_HEIGHT = 10;

    private final static int NORMAL_COLOR = Color.parseColor("#666666");
    private final static int SELECT_COLOR = Color.parseColor("#88CDE3");

    private int mNormalSize;
    private int mSelectSize;
    private int mMaxSize;
    private int mNormalColor;
    private int mSelectColor;
    private int mSpace;

    private Paint mNormalPaint;
    private Paint mSelectPaint;

    private RectF mRectF = new RectF();
    private float mDrawDistance = 0;
    private int mSelectIndex = 0;
    private int mPageWidth = 1920;

    private ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);
        mNormalColor = typedArray.getColor(R.styleable.PageIndicator_normal_color, NORMAL_COLOR);
        mSelectColor = typedArray.getColor(R.styleable.PageIndicator_select_color, SELECT_COLOR);
        mNormalSize = typedArray.getDimensionPixelSize(R.styleable.PageIndicator_normal_size, 10);
        mSelectSize = typedArray.getDimensionPixelSize(R.styleable.PageIndicator_select_size, 30);
        mMaxSize = typedArray.getInt(R.styleable.PageIndicator_max_count, 4);
        mSpace = typedArray.getDimensionPixelSize(R.styleable.PageIndicator_space, 10);
        typedArray.recycle();
    }


    public void init(int pageWidth) {
        mPageWidth = pageWidth;
    }

    public void setScrollDistance(int distanceX) {
        mDrawDistance = distanceX;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        for (int i = 0; i <= mMaxSize; i++) {
            canvas.drawRoundRect(
                    getRectF(i == mSelectIndex), mNormalSize / 2, mNormalSize / 2,
                    (i == mSelectIndex) ? mSelectPaint : mNormalPaint);
        }


        int selectIndex = (int) (mDrawDistance / mPageWidth);

        if (selectIndex == 0) {



        }


    }

    private RectF getRectF(boolean isSelected) {
        mRectF.setEmpty();
        float left = mDrawDistance == 0 ? mDrawDistance : mDrawDistance + mSpace;
        mRectF.set(left, 0,
                isSelected ? mSelectSize + left : mNormalSize + left, mNormalSize);
        mDrawDistance = mRectF.right;
        return mRectF;
    }


    private void initNormalPaint() {
        mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNormalPaint.setColor(mNormalColor);
    }

    private void initSelectPaint() {
        mSelectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSelectPaint.setColor(mSelectColor);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = measureSpec(widthMeasureSpec, DEFAULT_WIDTH);
        int heightSize = measureSpec(heightMeasureSpec, DEFAULT_HEIGHT);
        setMeasuredDimension(widthSize, heightSize);
        initNormalPaint();
        initSelectPaint();
    }

    private int measureSpec(int measureSpec, int defaultSize) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }


}
