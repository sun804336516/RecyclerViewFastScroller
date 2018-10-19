package com.sxw.recyclerview_fastscroller.fast_scroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.sxw.recyclerview_fastscroller.R;

public class FastScrollRecyclerView extends RecyclerView {

    private FastScroller mFastScroller;
    private boolean canFastScroll = true;
    private StateListDrawable mVerticalThumbDrawable;
    private Drawable mVerticalTrackDrawable;
    private StateListDrawable mHorizontalThumbDrawable;
    private Drawable mHorizontalTrackDrawable;
    /**
     * 显示快速滑动的最小屏幕数
     */
    private int minScreenCountScollerShow;
    private float paddingTop = 0;
    private float paddingBottom = 0;
    private float paddingLeft = 0;
    private float paddingRight = 0;
    private int popBackGroundColor;
    private int popTextColor;
    private float popTextSize;

    public FastScrollRecyclerView(Context context) {
        this(context, null);
    }

    public FastScrollRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScrollRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FastScrollRecyclerView);
        try {
            canFastScroll = typedArray.getBoolean(R.styleable.FastScrollRecyclerView_CanFastScroll, false);
            if (canFastScroll) {
                mVerticalThumbDrawable = (StateListDrawable) typedArray.getDrawable(R.styleable.FastScrollRecyclerView_VerticalThumbDrawable);
                mVerticalTrackDrawable = typedArray.getDrawable(R.styleable.FastScrollRecyclerView_VerticalTrackDrawable);
                mHorizontalThumbDrawable = (StateListDrawable) typedArray.getDrawable(R.styleable.FastScrollRecyclerView_HorizontalThumbDrawable);
                mHorizontalTrackDrawable = typedArray.getDrawable(R.styleable.FastScrollRecyclerView_HorizontalTrackDrawable);

                minScreenCountScollerShow = typedArray.getInteger(R.styleable.FastScrollRecyclerView_MinScreenCountScrollerShow, 4);
                paddingTop = typedArray.getDimension(R.styleable.FastScrollRecyclerView_ScrollerPaddingTop, 0);
                paddingBottom = typedArray.getDimension(R.styleable.FastScrollRecyclerView_ScrollerPaddingBottom, 0);
                paddingLeft = typedArray.getDimension(R.styleable.FastScrollRecyclerView_ScrollerPaddingLeft, 0);
                paddingRight = typedArray.getDimension(R.styleable.FastScrollRecyclerView_ScrollerPaddingRight, 0);

                popBackGroundColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_popBackGroundColor, 0x333333);
                popTextColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_popTextColor, 0xffffff);
                popTextSize = typedArray.getDimension(R.styleable.FastScrollRecyclerView_popTextSize, 22);

                mFastScroller = new FastScroller(this, mVerticalThumbDrawable, mVerticalTrackDrawable
                        , mHorizontalThumbDrawable, mHorizontalTrackDrawable);
                mFastScroller.setMinScreenCountScollerShow(minScreenCountScollerShow);
                mFastScroller.setPaddingTop(paddingTop);
                mFastScroller.setPaddingBottom(paddingBottom);
                mFastScroller.setPaddingLeft(paddingLeft);
                mFastScroller.setPaddingRight(paddingRight);
                mFastScroller.setPopBackGroundColor(popBackGroundColor);
                mFastScroller.setPopTextColor(popTextColor);
                mFastScroller.setPopTextSize(popTextSize);
            }
        } finally {
            typedArray.recycle();
        }
    }

    public void setVerticalLinearLayoutmanager() {
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void setVerticalGridLayoutManager(int count) {
        setLayoutManager(new GridLayoutManager(getContext(), count, LinearLayoutManager.VERTICAL, false));
    }

    public FastScroller getFastScroller() {
        return mFastScroller;
    }

}