/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sxw.recyclerview_fastscroller.fast_scroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class responsible to animate and provide a fast scroller.
 */
public class FastScroller extends ItemDecoration implements OnItemTouchListener {

    @IntDef({STATE_HIDDEN, STATE_VISIBLE, STATE_DRAGGING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    // Scroll thumb not showing
    private static final int STATE_HIDDEN = 0;
    // Scroll thumb visible and moving along with the scrollbar
    private static final int STATE_VISIBLE = 1;
    // Scroll thumb being dragged by user
    private static final int STATE_DRAGGING = 2;

    @IntDef({DRAG_X, DRAG_Y, DRAG_NONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DragState {
    }

    private static final int DRAG_NONE = 0;
    private static final int DRAG_X = 1;
    private static final int DRAG_Y = 2;

    @IntDef({ANIMATION_STATE_OUT, ANIMATION_STATE_FADING_IN, ANIMATION_STATE_IN,
            ANIMATION_STATE_FADING_OUT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface AnimationState {
    }

    private static final int ANIMATION_STATE_OUT = 0;
    private static final int ANIMATION_STATE_FADING_IN = 1;
    private static final int ANIMATION_STATE_IN = 2;
    private static final int ANIMATION_STATE_FADING_OUT = 3;

    private static final int SHOW_DURATION_MS = 500;
    private static final int HIDE_DELAY_AFTER_VISIBLE_MS = 2500;
    private static final int HIDE_DELAY_AFTER_DRAGGING_MS = 2500;
    private static final int HIDE_DURATION_MS = 500;
    private static final int SCROLLBAR_FULL_OPAQUE = 255;

    private static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    private static final int[] EMPTY_STATE_SET = new int[]{};

    private final int mScrollbarMinimumRange;
    private final int mMargin;

    // Final values for the vertical scroll bar
    private final StateListDrawable mVerticalThumbDrawable;
    private final Drawable mVerticalTrackDrawable;
    private final int mVerticalThumbWidth;
    private final int mVerticalTrackWidth;

    // Final values for the horizontal scroll bar
    private final StateListDrawable mHorizontalThumbDrawable;
    private final Drawable mHorizontalTrackDrawable;
    private final int mHorizontalThumbHeight;
    private final int mHorizontalTrackHeight;

    // Dynamic values for the vertical scroll bar
    int mVerticalThumbHeight;
    int mVerticalThumbCenterY;
    float mVerticalDragY;

    // Dynamic values for the horizontal scroll bar
    int mHorizontalThumbWidth;
    int mHorizontalThumbCenterX;
    float mHorizontalDragX;

    private int mRecyclerViewWidth = 0;
    private int mRecyclerViewHeight = 0;

    private RecyclerView mRecyclerView;
    /**
     * Whether the document is long/wide enough to require scrolling. If not, we don't show the
     * relevant scroller.
     */
    private boolean mNeedVerticalScrollbar = false;
    private boolean mNeedHorizontalScrollbar = false;
    @State
    private int mState = STATE_HIDDEN;
    @DragState
    private int mDragState = DRAG_NONE;

    private final int[] mVerticalRange = new int[2];
    private final int[] mHorizontalRange = new int[2];
    private final ValueAnimator mShowHideAnimator = ValueAnimator.ofFloat(0, 1);
    @AnimationState
    private int mAnimationState = ANIMATION_STATE_OUT;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide(HIDE_DURATION_MS);
        }
    };

    private PopVerticalDrawable mPopVerticalDrawable;
    /**
     * 显示快速滑动的最小屏幕数
     */
    private int minScreenCountScollerShow = 4;
    private float paddingTop = 0;
    private float paddingBottom = 0;
    private float paddingLeft = 0;
    private float paddingRight = 0;
    private int popBackGroundColor = 0x333333;
    private int popTextColor = 0xffffff;
    private float popTextSize = 22;

    public void setMinScreenCountScollerShow(int minScreenCountScollerShow) {
        if (minScreenCountScollerShow < this.minScreenCountScollerShow) {
            return;
        }
        this.minScreenCountScollerShow = minScreenCountScollerShow;
    }

    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public void setPopBackGroundColor(int popBackGroundColor) {
        this.popBackGroundColor = popBackGroundColor;
        mPopVerticalDrawable.setPopBgColor(popBackGroundColor);
    }

    public void setPopTextColor(int popTextColor) {
        this.popTextColor = popTextColor;
        mPopVerticalDrawable.setPopTextColor(popTextColor);
    }

    public void setPopTextSize(float popTextSize) {
        this.popTextSize = popTextSize;
        mPopVerticalDrawable.setTextSize(popTextSize);
    }

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int verticalContentLength = mRecyclerView.computeVerticalScrollRange();
            int verticalVisibleLength = mRecyclerViewHeight;
            if (verticalVisibleLength > 0 && verticalContentLength / verticalVisibleLength >= minScreenCountScollerShow) {
                updateScrollPosition(recyclerView.computeHorizontalScrollOffset(),
                        recyclerView.computeVerticalScrollOffset());
            } else {
                hide();
            }
        }
    };

    public boolean canPullDownOrUpToLoad() {
        return mState != STATE_DRAGGING;
    }

    public FastScroller(RecyclerView recyclerView, StateListDrawable verticalThumbDrawable,
                        Drawable verticalTrackDrawable, StateListDrawable horizontalThumbDrawable,
                        Drawable horizontalTrackDrawable) {
        mVerticalThumbDrawable = verticalThumbDrawable;
        mVerticalTrackDrawable = verticalTrackDrawable;
        mHorizontalThumbDrawable = horizontalThumbDrawable;
        mHorizontalTrackDrawable = horizontalTrackDrawable;

        Resources resources = recyclerView.getContext().getResources();
        int defaultWidth = resources.getDimensionPixelSize(android.support.v7.recyclerview.R.dimen.fastscroll_default_thickness);
        int scrollbarMinimumRange = resources.getDimensionPixelSize(android.support.v7.recyclerview.R.dimen.fastscroll_minimum_range);
        int margin = resources.getDimensionPixelOffset(android.support.v7.recyclerview.R.dimen.fastscroll_margin);

        mVerticalThumbWidth = Math.max(defaultWidth, verticalThumbDrawable.getIntrinsicWidth());
        mVerticalTrackWidth = Math.max(defaultWidth, verticalTrackDrawable.getIntrinsicWidth());
        mVerticalThumbHeight = Math.max(defaultWidth, verticalThumbDrawable.getIntrinsicHeight());
        mVerticalThumbCenterY = mVerticalThumbHeight / 2;

        mHorizontalThumbHeight = Math.max(defaultWidth, horizontalThumbDrawable.getIntrinsicHeight());
        mHorizontalTrackHeight = Math.max(defaultWidth, horizontalTrackDrawable.getIntrinsicHeight());
        mHorizontalThumbWidth = Math.max(defaultWidth, horizontalThumbDrawable.getIntrinsicWidth());
        mHorizontalThumbCenterX = mHorizontalThumbWidth / 2;

        mScrollbarMinimumRange = scrollbarMinimumRange;
        mMargin = margin;

        mShowHideAnimator.addListener(new AnimatorListener());
        mShowHideAnimator.addUpdateListener(new AnimatorUpdater());

        attachToRecyclerView(recyclerView);
        mPopVerticalDrawable = new PopVerticalDrawable();
        mPopVerticalDrawable.setHeight(mVerticalThumbHeight);

        mVerticalThumbDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
        mVerticalTrackDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
        mPopVerticalDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(this);
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        cancelHide();
    }

    private void requestRedraw() {
        mRecyclerView.invalidate();
    }

    private void setState(@State int state) {
        if (state == STATE_DRAGGING && mState != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(PRESSED_STATE_SET);
            cancelHide();
        }

        if (state == STATE_HIDDEN) {
            requestRedraw();
        } else {
            show();
        }

        if (mState == STATE_DRAGGING && state != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(EMPTY_STATE_SET);
            resetHideDelay(HIDE_DELAY_AFTER_DRAGGING_MS);
        } else if (state == STATE_VISIBLE) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS);
        }
        mState = state;
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(mRecyclerView) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public boolean isDragging() {
        return mState == STATE_DRAGGING;
    }

    boolean isVisible() {
        return mState == STATE_VISIBLE;
    }

    boolean isHidden() {
        return mState == STATE_HIDDEN;
    }


    public void show() {
        switch (mAnimationState) {
            case ANIMATION_STATE_FADING_OUT:
                mShowHideAnimator.cancel();
                // fall through
            case ANIMATION_STATE_OUT:
                mAnimationState = ANIMATION_STATE_FADING_IN;
                mShowHideAnimator.setFloatValues((float) mShowHideAnimator.getAnimatedValue(), 1);
                mShowHideAnimator.setDuration(SHOW_DURATION_MS);
                mShowHideAnimator.setStartDelay(0);
                mShowHideAnimator.start();
                break;
        }
    }

    public void hide() {
        hide(0);
    }


    void hide(int duration) {
        switch (mAnimationState) {
            case ANIMATION_STATE_FADING_IN:
                mShowHideAnimator.cancel();
                // fall through
            case ANIMATION_STATE_IN:
                mAnimationState = ANIMATION_STATE_FADING_OUT;
                mShowHideAnimator.setFloatValues((float) mShowHideAnimator.getAnimatedValue(), 0);
                mShowHideAnimator.setDuration(duration);
                mShowHideAnimator.start();
                break;
        }
    }

    private void cancelHide() {
        mRecyclerView.removeCallbacks(mHideRunnable);
    }

    private void resetHideDelay(int delay) {
        cancelHide();
        mRecyclerView.postDelayed(mHideRunnable, delay);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mRecyclerViewWidth != mRecyclerView.getWidth()
                || mRecyclerViewHeight != mRecyclerView.getHeight()) {
            mRecyclerViewWidth = mRecyclerView.getWidth();
            mRecyclerViewHeight = mRecyclerView.getHeight();
            // This is due to the different events ordering when keyboard is opened or
            // retracted vs rotate. Hence to avoid corner cases we just disable the
            // scroller when size changed, and wait until the scroll position is recomputed
            // before showing it back.
            setState(STATE_HIDDEN);
            return;
        }
        if (mAnimationState != ANIMATION_STATE_OUT) {

            if (mNeedVerticalScrollbar) {
                drawVerticalScrollbar(canvas);
            }
            if (mNeedHorizontalScrollbar) {
                drawHorizontalScrollbar(canvas);
            }
        }
    }

    private void drawVerticalScrollbar(Canvas canvas) {
        int viewWidth = mRecyclerViewWidth;

        int left = viewWidth - mVerticalThumbWidth;
        int top = mVerticalThumbCenterY - mVerticalThumbHeight / 2;
        mVerticalThumbDrawable.setBounds(0, 0, mVerticalThumbWidth, mVerticalThumbHeight);
        mVerticalTrackDrawable.setBounds(0, 0, mVerticalTrackWidth, mRecyclerViewHeight);

        int save = canvas.save();
        mPopVerticalDrawable.drawVertical(canvas, left, mVerticalThumbCenterY);
        canvas.restoreToCount(save);

        if (isLayoutRTL()) {
            mVerticalTrackDrawable.draw(canvas);
            canvas.translate(mVerticalThumbWidth, top);
            canvas.scale(-1, 1);
            mVerticalThumbDrawable.draw(canvas);
            canvas.scale(1, 1);
            canvas.translate(-mVerticalThumbWidth, -top);
        } else {
            canvas.translate(left, 0);
            mVerticalTrackDrawable.draw(canvas);
            canvas.translate(0, top);
            mVerticalThumbDrawable.draw(canvas);
            canvas.translate(-left, -top);

        }
    }

    private void drawHorizontalScrollbar(Canvas canvas) {
        int viewHeight = mRecyclerViewHeight;

        int top = viewHeight - mHorizontalThumbHeight;
        int left = mHorizontalThumbCenterX - mHorizontalThumbWidth / 2;
        mHorizontalThumbDrawable.setBounds(0, 0, mHorizontalThumbWidth, mHorizontalThumbHeight);
        mHorizontalTrackDrawable.setBounds(0, 0, mRecyclerViewWidth, mHorizontalTrackHeight);

        canvas.translate(0, top);
        mHorizontalTrackDrawable.draw(canvas);
        canvas.translate(left, 0);
        mHorizontalThumbDrawable.draw(canvas);
        canvas.translate(-left, -top);
    }

    /**
     * Notify the scroller of external change of the scroll, e.g. through dragging or flinging on
     * the view itself.
     *
     * @param offsetX The new scroll X offset.
     * @param offsetY The new scroll Y offset.
     */
    public void updateScrollPosition(int offsetX, int offsetY) {
        int verticalContentLength = mRecyclerView.computeVerticalScrollRange();
        int verticalVisibleLength = mRecyclerViewHeight;
        mNeedVerticalScrollbar = verticalContentLength - verticalVisibleLength > 0
                && mRecyclerViewHeight >= mScrollbarMinimumRange;

        int horizontalContentLength = mRecyclerView.computeHorizontalScrollRange();
        int horizontalVisibleLength = mRecyclerViewWidth;
        mNeedHorizontalScrollbar = horizontalContentLength - horizontalVisibleLength > 0
                && mRecyclerViewWidth >= mScrollbarMinimumRange;

        if (!mNeedVerticalScrollbar && !mNeedHorizontalScrollbar) {
            if (mState != STATE_HIDDEN) {
                setState(STATE_HIDDEN);
            }
            return;
        }
        if (mNeedVerticalScrollbar) {
            int mVerticalCenterYmin = (int) (mVerticalThumbHeight / 2 + paddingTop);
            int mVerticalCenterYMax = (int) (verticalVisibleLength - mVerticalThumbHeight / 2 - paddingBottom);

            float middleScreenPos = offsetY + verticalVisibleLength / 2.0f;
            float fractionVer = middleScreenPos / verticalContentLength;
            mVerticalThumbCenterY = (int) (verticalVisibleLength * fractionVer);
            if (mVerticalThumbCenterY < mVerticalCenterYmin) {
                mVerticalThumbCenterY = mVerticalCenterYmin;
            }
            if (mVerticalThumbCenterY > mVerticalCenterYMax) {
                mVerticalThumbCenterY = mVerticalCenterYMax;
            }
            if (!mRecyclerView.canScrollVertically(-1)) {
                mVerticalThumbCenterY = mVerticalCenterYmin;
            } else if (!mRecyclerView.canScrollVertically(1)) {
                mVerticalThumbCenterY = mVerticalCenterYMax;
            }

//            mVerticalThumbHeight = Math.min(mVerticalThumbDrawable.getIntrinsicHeight(),
//                    (verticalVisibleLength * verticalVisibleLength) / verticalContentLength);
        }

        if (mNeedHorizontalScrollbar) {
            int mHorizontalCenterYmin = (int) (mHorizontalThumbWidth / 2 + paddingLeft);
            int mHorizontalCenterYmax = (int) (horizontalVisibleLength - mHorizontalThumbWidth / 2 - paddingRight);

            float middleScreenPos = offsetX + horizontalVisibleLength / 2.0f;
            float fractionHori = middleScreenPos / horizontalContentLength;
            mHorizontalThumbCenterX = (int) (horizontalVisibleLength * fractionHori);

            if (mHorizontalThumbCenterX < mHorizontalCenterYmin) {
                mHorizontalThumbCenterX = mHorizontalCenterYmin;
            }
            if (mHorizontalThumbCenterX > mHorizontalCenterYmax) {
                mHorizontalThumbCenterX = mHorizontalCenterYmax;
            }

            if (!mRecyclerView.canScrollHorizontally(-1)) {
                mVerticalThumbCenterY = mHorizontalCenterYmin;
            } else if (!mRecyclerView.canScrollHorizontally(1)) {
                mVerticalThumbCenterY = mHorizontalCenterYmax;
            }
//            mHorizontalThumbWidth = Math.min(mVerticalThumbDrawable.getIntrinsicWidth(),
//                    (horizontalVisibleLength * horizontalVisibleLength) / horizontalContentLength);
        }
        if (mState == STATE_HIDDEN || mState == STATE_VISIBLE) {
            setState(STATE_VISIBLE);
        }

        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter != null && adapter instanceof SelectNameAdapter) {

            int firstPosition = 0, lastPosition = 0;
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                firstPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) layoutManager;
                int[] firstVisiableCount = new int[manager.getSpanCount()];
                manager.findFirstVisibleItemPositions(firstVisiableCount);
                int[] lastVisiableCount = new int[manager.getSpanCount()];
                manager.findLastVisibleItemPositions(lastVisiableCount);

                if (firstVisiableCount.length > 0) {
                    firstPosition = firstVisiableCount[0];
                }
                if (lastVisiableCount.length > 0) {
                    lastPosition = lastVisiableCount[lastVisiableCount.length - 1];
                }
            }

            int position = lastPosition;
            int thumbStartY = mVerticalThumbCenterY - mHorizontalThumbHeight / 2;
            for (int i = firstPosition; i < lastPosition; i++) {
                View viewByPosition = layoutManager.findViewByPosition(i);
                if (thumbStartY <= viewByPosition.getBottom()) {
                    position = i;
                    break;
                }
            }
            String popTextName = ((SelectNameAdapter) adapter).getSelectName(position);
            mPopVerticalDrawable.setPopStr(popTextName);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent ev) {
        final boolean handled;
        if (mState == STATE_VISIBLE) {
            boolean insideVerticalThumb = isPointInsideVerticalThumb(ev.getX(), ev.getY());
            boolean insideHorizontalThumb = isPointInsideHorizontalThumb(ev.getX(), ev.getY());
            if (ev.getAction() == MotionEvent.ACTION_DOWN
                    && (insideVerticalThumb || insideHorizontalThumb)) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X;
                    mHorizontalDragX = (int) ev.getX();
                } else if (insideVerticalThumb) {
                    mDragState = DRAG_Y;
                    mVerticalDragY = (int) ev.getY();
                }

                setState(STATE_DRAGGING);
                handled = true;
            } else {
                handled = false;
            }
        } else if (mState == STATE_DRAGGING) {
            handled = true;
        } else {
            handled = false;
        }
        return handled;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent me) {
        if (mState == STATE_HIDDEN) {
            return;
        }

        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            boolean insideVerticalThumb = isPointInsideVerticalThumb(me.getX(), me.getY());
            boolean insideHorizontalThumb = isPointInsideHorizontalThumb(me.getX(), me.getY());
            if (insideVerticalThumb || insideHorizontalThumb) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X;
                    mHorizontalDragX = (int) me.getX();
                } else if (insideVerticalThumb) {
                    mDragState = DRAG_Y;
                    mVerticalDragY = (int) me.getY();
                }
                setState(STATE_DRAGGING);
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP && mState == STATE_DRAGGING) {
            mVerticalDragY = 0;
            mHorizontalDragX = 0;
            setState(STATE_VISIBLE);
            mDragState = DRAG_NONE;
        } else if (me.getAction() == MotionEvent.ACTION_MOVE && mState == STATE_DRAGGING) {
            show();

            if (mDragState == DRAG_X) {
                horizontalScrollTo(me.getX());
            }
            if (mDragState == DRAG_Y) {
                verticalScrollTo(me.getY());
            }
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private void verticalScrollTo(float y) {
        final int[] scrollbarRange = getVerticalRange();
        y = Math.max(scrollbarRange[0], Math.min(scrollbarRange[1], y));
        if (Math.abs(mVerticalThumbCenterY - y) < 2) {
            return;
        }
        int scrollingBy = scrollTo(mVerticalDragY, y, scrollbarRange,
                mRecyclerView.computeVerticalScrollRange(),
                mRecyclerView.computeVerticalScrollOffset(), mRecyclerViewHeight);


        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(0, scrollingBy);
        }
        mVerticalDragY = y;
    }

    private void horizontalScrollTo(float x) {
        final int[] scrollbarRange = getHorizontalRange();
        x = Math.max(scrollbarRange[0], Math.min(scrollbarRange[1], x));
        if (Math.abs(mHorizontalThumbCenterX - x) < 2) {
            return;
        }

        int scrollingBy = scrollTo(mHorizontalDragX, x, scrollbarRange,
                mRecyclerView.computeHorizontalScrollRange(),
                mRecyclerView.computeHorizontalScrollOffset(), mRecyclerViewWidth);
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(scrollingBy, 0);
        }

        mHorizontalDragX = x;
    }

    private int scrollTo(float oldDragPos, float newDragPos, int[] scrollbarRange, int scrollRange,
                         int scrollOffset, int viewLength) {
        int scrollbarLength = scrollbarRange[1] - scrollbarRange[0];
        if (scrollbarLength == 0) {
            return 0;
        }
        float percentage = ((newDragPos - oldDragPos) / (float) scrollbarLength);

//        int totalPossibleOffset = scrollRange - viewLength;
        int totalPossibleOffset = scrollRange;
        int scrollingBy = (int) (percentage * totalPossibleOffset);
        int absoluteOffset = scrollOffset + scrollingBy;
        if (absoluteOffset < totalPossibleOffset && absoluteOffset >= 0) {
            return scrollingBy;
        } else {
            return 0;
        }
    }


    boolean isPointInsideVerticalThumb(float x, float y) {
        return (isLayoutRTL() ? x <= mVerticalThumbWidth / 2
                : x >= mRecyclerViewWidth - mVerticalThumbWidth)
                && y >= mVerticalThumbCenterY - mVerticalThumbHeight / 2
                && y <= mVerticalThumbCenterY + mVerticalThumbHeight / 2;
    }


    boolean isPointInsideHorizontalThumb(float x, float y) {
        return (y >= mRecyclerViewHeight - mHorizontalThumbHeight)
                && x >= mHorizontalThumbCenterX - mHorizontalThumbWidth / 2
                && x <= mHorizontalThumbCenterX + mHorizontalThumbWidth / 2;
    }


    Drawable getHorizontalTrackDrawable() {
        return mHorizontalTrackDrawable;
    }


    Drawable getHorizontalThumbDrawable() {
        return mHorizontalThumbDrawable;
    }


    Drawable getVerticalTrackDrawable() {
        return mVerticalTrackDrawable;
    }


    Drawable getVerticalThumbDrawable() {
        return mVerticalThumbDrawable;
    }

    /**
     * Gets the (min, max) vertical positions of the vertical scroll bar.
     */
    private int[] getVerticalRange() {
        mVerticalRange[0] = mMargin;
        mVerticalRange[1] = mRecyclerViewHeight - mMargin;
        return mVerticalRange;
    }

    /**
     * Gets the (min, max) horizontal positions of the horizontal scroll bar.
     */
    private int[] getHorizontalRange() {
        mHorizontalRange[0] = mMargin;
        mHorizontalRange[1] = mRecyclerViewWidth - mMargin;
        return mHorizontalRange;
    }

    private class AnimatorListener extends AnimatorListenerAdapter {

        private boolean mCanceled = false;

        @Override
        public void onAnimationEnd(Animator animation) {
            // Cancel is always followed by a new directive, so don't update state.
            if (mCanceled) {
                mCanceled = false;
                return;
            }
            if ((float) mShowHideAnimator.getAnimatedValue() == 0) {
                mAnimationState = ANIMATION_STATE_OUT;
                setState(STATE_HIDDEN);
            } else {
                mAnimationState = ANIMATION_STATE_IN;
                requestRedraw();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mCanceled = true;
        }
    }

    private class AnimatorUpdater implements AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int alpha = (int) (SCROLLBAR_FULL_OPAQUE * ((float) valueAnimator.getAnimatedValue()));
            mVerticalThumbDrawable.setAlpha(alpha);
            mVerticalTrackDrawable.setAlpha(alpha);
            mPopVerticalDrawable.setAlpha(alpha);
            requestRedraw();
        }
    }
}
