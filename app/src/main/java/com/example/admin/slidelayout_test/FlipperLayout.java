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

public class FlipperLayout extends FrameLayout {

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mVelocityValue = 0;

    /** 商定这个滑动是否有效的距离 */
    private int limitDistance = 0;

    private int screenWidth = 0;

    /** 手指移动的方向 */
    private static final int MOVE_TO_LEFT = 0;
    private static final int MOVE_TO_RIGHT = 1;
    private static final int MOVE_NO_RESULT = 2;

    /** 最后触摸的结果方向 */
    private int mTouchResult = MOVE_NO_RESULT;
    /** 一开始的方向 */
    private int mDirection = MOVE_NO_RESULT;

    /** 触摸的模式 */
    private static final int MODE_NONE = 0;
    private static final int MODE_MOVE = 1;

    private int mMode = MODE_NONE;

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
        limitDistance = screenWidth / 3;
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
        currentTopView.scrollTo(-screenWidth, 0);
        currentTopView.setTranslationX(-screenWidth);
    }


//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        for (int i = 0; i < getChildCount(); i++) {
////            System.out.println("on layout :" + i);
//            View child = getChildAt(i);
//            int height = child.getMeasuredHeight();
//            int width = child.getMeasuredWidth();
//
////            System.out.printf("onlayout child %d  width = %s, height = %s.",i,width,height);
////            System.out.println();
//
//            child.layout(0, 0, width, height);
//        }
//    }
//
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        System.out.printf("onmeasure widthmeasurespec = %s,and heightmeasurespec = %s.",widthMeasureSpec,heightMeasureSpec);
//        System.out.println();
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
//                    System.out.println("ontouchevent show mscroller is scrolling.");

                    return super.onTouchEvent(event);
                }
//                System.out.println("ontouchevent show mscroller is finished.");
                if (startX == 0) {
//                    System.out.println("i think here was not used,and answer is YES!");
                    startX = (int) event.getX();
                }
                final int distance = startX - (int) event.getX();
//                System.out.println("first we decide the direction");
                if (mDirection == MOVE_NO_RESULT) {
                    if (mListener.whetherHasNextPage() && distance > 0) {
                        mDirection = MOVE_TO_LEFT;
//                        System.out.println("mdirection is move_to_left");
                    } else if (mListener.whetherHasPreviousPage() && distance < 0) {
                        mDirection = MOVE_TO_RIGHT;
//                        System.out.println("mdirection is move_to_right");
                    }
                }

//                System.out.println("and then is the mode.");
                if (mMode == MODE_NONE
                        && ((mDirection == MOVE_TO_LEFT && mListener.whetherHasNextPage()) || (mDirection == MOVE_TO_RIGHT && mListener
                        .whetherHasPreviousPage()))) {
                    mMode = MODE_MOVE;
//                    System.out.println("mode is mode_move");
                }

                if (mMode == MODE_MOVE) {
                    if ((mDirection == MOVE_TO_LEFT && distance <= 0) || (mDirection == MOVE_TO_RIGHT && distance >= 0)) {
                        mMode = MODE_NONE;
//                        System.out.println("mode is mode_none");
                    }
                }

                if (mDirection != MOVE_NO_RESULT) {
//                    System.out.println("here we decide which view to scroll");
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
//                        System.out.println("mode is mode_move,so move!");
                        mVelocityTracker.computeCurrentVelocity(1000, ViewConfiguration.getMaximumFlingVelocity());
                        if (mDirection == MOVE_TO_LEFT) {
                            mScrollerView.setTranslationX(distance);
                        } else {
                            mScrollerView.setTranslationX(screenWidth + distance);
                        }
                    } else {
//                        System.out.println("so when mode is not mode_move? and what will we do?");
                        final int scrollX = mScrollerView.getScrollX();
                        if (mDirection == MOVE_TO_LEFT && scrollX != 0 && mListener.whetherHasNextPage()) {
                            mScrollerView.setTranslationX(0);
                        } else if (mDirection == MOVE_TO_RIGHT && mListener.whetherHasPreviousPage() && screenWidth != Math.abs(scrollX)) {
                            mScrollerView.setTranslationX(-screenWidth);
                        }

                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                Log.i("mandy" ,"action_up :" + mScrollerView);
                if (mScrollerView == null) {
                    return super.onTouchEvent(event);
                }
                final int scrollX = mScrollerView.getScrollX();
                Log.i("mandy" ,"scrollX :" + scrollX);
                mVelocityValue = (int) mVelocityTracker.getXVelocity();
                // scroll左正，右负(),(startX + dx)的值如果为0，即复位
            /*
             * android.widget.Scroller.startScroll( int startX, int startY, int
             * dx, int dy, int duration )
             */

                int time = 500;

                if (mMode == MODE_MOVE && mDirection == MOVE_TO_LEFT) {
                    if (scrollX > limitDistance || mVelocityValue < -time) {
                        // 手指向左移动，可以翻屏幕
                        mTouchResult = MOVE_TO_LEFT;
                        if (mVelocityValue < -time) {
                            time = 200;
                        }
                        mScroller.startScroll(scrollX, 0, screenWidth - scrollX, 0, time);
                    } else {
                        mTouchResult = MOVE_NO_RESULT;
                        mScroller.startScroll(scrollX, 0, -scrollX, 0, time);
                    }
                } else if (mMode == MODE_MOVE && mDirection == MOVE_TO_RIGHT) {
                    if ((screenWidth - scrollX) > limitDistance || mVelocityValue > time) {
                        // 手指向右移动，可以翻屏幕
                        mTouchResult = MOVE_TO_RIGHT;
                        if (mVelocityValue > time) {
                            time = 250;
                        }
                        mScroller.startScroll(scrollX, 0, -scrollX, 0, time);
                    } else {
                        mTouchResult = MOVE_NO_RESULT;
                        mScroller.startScroll(scrollX, 0, screenWidth - scrollX, 0, time);
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
            mScrollerView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
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
                    currentTopView.scrollTo(-screenWidth, 0);
                    addView(currentTopView);
                } else {
                    final View newView = mListener.createView(mTouchResult);
//                    currentTopView = new View(getContext());
                    currentTopView = newView;
                    Log.i("mandy" ,"currentView :" + newView);
                    currentTopView.scrollTo(-screenWidth, 0);
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
        public View createView(final int direction);

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