package com.example.admin.slidelayout_test;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class FlipperLayout extends ViewGroup {

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mVelocityValue = 0;

    /** 商定这个滑动是否有效的距离 */
    private int limitDistance = 20;

    private int screenWidth = 0;
    private int leftline = 0;
    private int rightline = 0;

    /** 手指移动的方向 */
    private static final String MOVE_TO_LEFT = "MOVE_TO_LEFT";
    private static final String MOVE_TO_RIGHT = "MOVE_TO_RIGHT";
    private static final String MOVE_NO_RESULT = "MOVE_NO_RESULT";

    /** 最后触摸的结果方向 */
    private String mTouchResult = MOVE_NO_RESULT;
    /** 一开始的方向 */
    private String mDirection = MOVE_NO_RESULT;

    /** 触摸的模式 */
    private static final String MODE_NONE = "MODE_NONE";
    private static final String MODE_MOVE = "MODE_MOVE";

    private String mMode = MODE_NONE;

    /** 滑动的view */
    private View mScrollerView = null;

    /** 最上层的view（处于边缘的，看不到的） */
    private View currentTopView = null;

    /** 显示的view，显示在屏幕 */
    private View currentShowView = null;

    /** 最底层的view（看不到的） */
    private View currentBottomView = null;

    public FlipperLayout(Context context) {
        super(context);
        init(context);
    }

    public FlipperLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FlipperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        leftline = screenWidth/3;
        rightline = screenWidth/3*2;
    }

    /***
     *
     * @param listener
     * @param currentBottomView
     *      最底层的view，初始状态看不到
     * @param currentShowView
     *      正在显示的View
     * @param currentTopView
     *      最上层的View，初始化时滑出屏幕
     */
    public void initFlipperViews(TouchListener listener, View currentBottomView, View currentShowView, View currentTopView) {
        this.currentBottomView = currentBottomView;
        this.currentShowView = currentShowView;
        this.currentTopView = currentTopView;
        setTouchResultListener(listener);
        addView(currentBottomView);
        addView(currentShowView);
        addView(currentTopView);
        /** 默认将最上层的view滑动的边缘（用于查看上一页） */
        currentTopView.setTranslationX(-screenWidth);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int height = child.getMeasuredHeight();
            int width = child.getMeasuredWidth();

            child.layout(0, 0, width, height);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int startX = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    break;
                }
                startX = (int) ev.getX();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!mScroller.isFinished()) {
                    return super.onTouchEvent(event);
                }
                if (startX == 0) {
                    startX = (int) event.getX();
                }
                final int distance = (int) event.getX() - startX;
                if (mDirection == MOVE_NO_RESULT) {
                    if (mListener.whetherHasNextPage() && distance < 0) {
                        mDirection = MOVE_TO_LEFT;
                    } else if (mListener.whetherHasPreviousPage() && distance > 0) {
                        mDirection = MOVE_TO_RIGHT;
                    }
                }

                if (mMode == MODE_NONE
                        && ((mDirection == MOVE_TO_LEFT && mListener.whetherHasNextPage()) || (mDirection == MOVE_TO_RIGHT && mListener
                        .whetherHasPreviousPage()))) {
                    mMode = MODE_MOVE;
                }

//                if (mMode == MODE_MOVE) {
//                    if ((mDirection == MOVE_TO_LEFT && distance >= 0) || (mDirection == MOVE_TO_RIGHT && distance <= 0)) {
//                        mMode = MODE_NONE;
//                        System.out.println("mode is mode_none");
//                    }
//                }

                if (mDirection != MOVE_NO_RESULT) {
                    if (mDirection == MOVE_TO_LEFT) {
                        if (mScrollerView != currentShowView) {
                            mScrollerView = currentShowView;
                        }
                    } else {
                        if (mScrollerView != currentTopView) {
                            mScrollerView = currentTopView;
                        }
                    }
                    if (mMode == MODE_MOVE) {
                        mVelocityTracker.computeCurrentVelocity(1000, ViewConfiguration.getMaximumFlingVelocity());
                        if (mDirection == MOVE_TO_LEFT) {
                            mScrollerView.setTranslationX(distance);
                        } else {
                            mScrollerView.setTranslationX(distance-screenWidth);
                        }
                    }
//                    else {
//                        System.out.println("so when mode is not mode_move? and what will we do?");
//                        final int scrollX = mScrollerView.getScrollX();
//                        if (mDirection == MOVE_TO_LEFT && scrollX != 0 && mListener.whetherHasNextPage()) {
//                            mScrollerView.setTranslationX(-screenWidth);
//                        } else if (mDirection == MOVE_TO_RIGHT && mListener.whetherHasPreviousPage() && screenWidth != Math.abs(scrollX)) {
//                            mScrollerView.setTranslationX(screenWidth);
//                        }
//
//                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                if (mScrollerView == null) {
                    return super.onTouchEvent(event);
                }
                int durationtime = 300;
                /**
                 * 滑动时候
                 */
                final int scrollX = (int)mScrollerView.getX();
                mVelocityValue = (int) mVelocityTracker.getXVelocity();
                int time = 100;

                if (mMode == MODE_MOVE && mDirection == MOVE_TO_LEFT) {
                    if (-scrollX > limitDistance && mVelocityValue < -time) {
                        // 手指向左移动，可以翻屏幕
                        mTouchResult = MOVE_TO_LEFT;
                        mScroller.startScroll(scrollX, 0, -screenWidth-scrollX, 0, durationtime);
                    } else {
                        mTouchResult = MOVE_NO_RESULT;
                        mScroller.startScroll(scrollX, 0, 0-scrollX, 0, durationtime);
                    }
                } else {
                    if (mMode == MODE_MOVE && mDirection == MOVE_TO_RIGHT) {
                    if ((screenWidth + scrollX) > limitDistance && mVelocityValue > time) { //判断距离或者时间
                            // 手指向右移动，可以翻屏幕
                            mTouchResult = MOVE_TO_RIGHT;
                            mScroller.startScroll(scrollX, 0, -scrollX, 0, durationtime);
                        } else {
                            mTouchResult = MOVE_NO_RESULT;
                            mScroller.startScroll(scrollX, 0, -screenWidth - scrollX, 0, durationtime);
                        }
                    }
                }
                resetVariables();
                postInvalidate();
                break;
        }
        return true;
    }

    private void resetVariables() {
        mDirection = MOVE_NO_RESULT;
        mMode = MODE_NONE;
        startX = 0;
        releaseVelocityTracker();
    }

    private TouchListener mListener;

    private void setTouchResultListener(TouchListener listener) {
        this.mListener = listener;
    }

    @Override
    public void computeScroll() {

//        System.out.println("here computescroll is start.");

        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mScrollerView.setTranslationX(mScroller.getCurrX());
            postInvalidate();
        } else if (mScroller.isFinished() && mListener != null && mTouchResult != MOVE_NO_RESULT) {
//            System.out.println("and here computescroll was finished.");

            if (mTouchResult == MOVE_TO_LEFT) {
                if (currentTopView != null) {
                    removeView(currentTopView);
                }
                currentTopView = mScrollerView;
                currentShowView = currentBottomView;
                if (mListener.currentIsLastPage()) {
                    final View newView = mListener.createView(mTouchResult);
                    currentBottomView = newView;
                    addView(newView, 0);
                } else {
                    final View newView = mListener.createView(mTouchResult);
//                    currentBottomView = new View(getContext());
                    currentBottomView = newView;
//                    currentBottomView.setVisibility(View.GONE);
                    addView(currentBottomView, 0);
                }
            } else {
                Log.v("mandy","move to right");
                if (currentBottomView != null) {
                    removeView(currentBottomView);
                }
                currentBottomView = currentShowView;
                currentShowView = mScrollerView;
                if (mListener.currentIsFirstPage()) {
                    final View newView = mListener.createView(mTouchResult);
                    currentTopView = newView;
                    currentTopView.setTranslationX(-screenWidth);
                    addView(currentTopView);
                } else {
                    final View newView = mListener.createView(mTouchResult);
//                    currentTopView = new View(getContext());
                    currentTopView = newView;
                    Log.i("mandy" ,"currentView :" + newView);
                    currentTopView.setTranslationX(-screenWidth);
//                    currentTopView.setVisibility(View.GONE);
                    addView(currentTopView);
                }
            }
            mTouchResult = MOVE_NO_RESULT;
        }
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    /***
     * 用来实时回调触摸事件回调
     *
     * @author freeson
     */
    public interface TouchListener {

        /** 手指向左滑动，即查看下一章节 */
        final int MOVE_TO_LEFT = 0;
        /** 手指向右滑动，即查看上一章节 */
        final int MOVE_TO_RIGHT = 1;

        /**
         * 创建一个承载Text的View
         *
         * @param direction
         *            {@link MOVE_TO_LEFT,MOVE_TO_RIGHT}
         * @return
         */
        public View createView(final String direction);

        /***
         * 当前页是否是第一页
         *
         * @return
         */
        public boolean currentIsFirstPage();

        /***
         * 当前页是否是最后一页
         *
         * @return
         */
        public boolean currentIsLastPage();

        /**
         * 当前页是否有上一页（用来判断可滑动性）
         *
         * @return
         */
        public boolean whetherHasPreviousPage();

        /***
         * 当前页是否有下一页（用来判断可滑动性）
         *
         * @return
         */
        public boolean whetherHasNextPage();
    }

}