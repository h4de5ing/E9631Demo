package com.unistrong.demo.gps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 仿写Launcher的workspace类 实现左右屏幕的滑动效果
 *
 * @author Administrator
 */
public class WorkSpace extends ViewGroup {

    private int mCurrentScrren; // 当前的显示的屏幕
    private int mDefaultScrren = 0; // 默认的显示的屏幕
    private int mTouchSlop; // 滑动多少的距离 显示下一个屏幕
    private int mCountScrren;  //总过多少屏

    private Scroller mScroller; // 滑动控制类

    private VelocityTracker mVelocityTracker;

    private float mLastMotionX;
    private float mLastMotionY;

    private static final int TOUCH_STATE_REST = 0;

    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int SNAP_VELOCITY = 600;

    private int mTouchState = TOUCH_STATE_REST;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;
        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance
                    : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    public WorkSpace(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        mCurrentScrren = mDefaultScrren;

        mScrollInterpolator = new WorkspaceOvershootInterpolator();

        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    }

    public WorkSpace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkSpace(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (changed) {
            int childLeft = 0;
            final int childCount = this.getChildCount();
            mCountScrren = childCount - 1;
            for (int i = 0; i < childCount; i++) {
                final View childView = this.getChildAt(i);

                if (childView.getVisibility() != View.GONE) {
                    final int childWidth = childView.getMeasuredWidth();
                    childView.layout(childLeft, 0, childLeft + childWidth,
                            childView.getMeasuredHeight());

                    childLeft += childWidth;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 得到规范化的参数
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        final int height = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(widthMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        scrollTo(mCurrentScrren * width, 0);
    }

    public void snapToScreen(int whichScreen) {
        // get the valid layout page
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != (whichScreen * getWidth())) {
            final int delta = whichScreen * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0,
                    delta, 0, Math.abs(delta) * 2);
            mCurrentScrren = whichScreen;
            invalidate();
        }
    }

    /**
     * 　　* According to the position of current layout
     * <p>
     * 　　* scroll to the destination page.
     */
    public void snapToDestination() {
        final int screenWidth = getWidth();
        final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
    }

    public void setToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        mCurrentScrren = whichScreen;
        scrollTo(whichScreen * getWidth(), 0);

    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished())
            mScroller.abortAnimation();
        mCurrentScrren = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScrren * getWidth(), 0);
        invalidate();
    }

    public int getCurrentScreen() {
        return mCurrentScrren;
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /**
         * 用来跟踪触摸速度的类 当你需要跟踪的时候使用obtain()方法来来获得VelocityTracker类的一个实例对象
         * 使用addMovement(MotionEvent)函数将当前的移动事件传递给VelocityTracker对象
         *
         * 使用computeCurrentVelocity (int units)函数来计算当前的速度
         *
         * 使用getXVelocity ()、getYVelocity ()函数来获得当前的速度
         */
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);
        final int action = event.getAction();

        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;
//			if ((mCurrentScrren == 0 && deltaX > 0)
//					|| (mCurrentScrren == 1 && deltaX < 0)) {
//				scrollBy(deltaX, 0);
//			}
                scrollBy(deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurrentScrren > 0) {
                    snapToScreen(mCurrentScrren - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScrren < mCountScrren) {
                    snapToScreen(mCurrentScrren + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) mTouchState = TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:

            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }
}
