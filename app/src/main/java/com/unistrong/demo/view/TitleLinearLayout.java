package com.unistrong.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.unistrong.demo.R;

public class TitleLinearLayout extends LinearLayout {
    public TitleLinearLayout(Context context) {
        this(context, null);
    }

    public TitleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleLinearLayout);
        mTitleText = a.getString(R.styleable.TitleLinearLayout_tipText);
        a.recycle();
    }

    private String mTitleText;
    private Paint mTitlePaint;
    private Paint mRectPaint;

    private void initAttrs() {
        mTitlePaint = new Paint();
        mTitlePaint.setColor(Color.RED);
        mTitlePaint.setAntiAlias(true);
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.BLACK);
    }

    private int mTop = 0;
    private int mBottom = 0;
    private int mLeft = 0;
    private int mRight = 0;
    private float mTextSize = 20;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeft = getLeft();
        mRight = getRight();
        mTop = getTop();
        mBottom = getBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);
        canvas.drawRect(mLeft, mTop + 10, mRight, mBottom, mRectPaint);
        if (!TextUtils.isEmpty(mTitleText)) {
            mTitlePaint.setTextSize(mTextSize);
            mTitlePaint.setColor(Color.WHITE);
            canvas.drawRect(mLeft, mTop, mTitleText.length() * mTextSize, mTextSize, mTitlePaint);
            mTitlePaint.setColor(Color.RED);
            canvas.drawText(mTitleText, 5 + mLeft, mTop + mTextSize, mTitlePaint);
        }
    }
}
