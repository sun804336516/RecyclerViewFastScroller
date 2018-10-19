package com.sxw.recyclerview_fastscroller.fast_scroller;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Administrator on 2018/10/16/016 12:25
 * 垂直方向的Pop  水平方向待完善
 */
public class PopVerticalDrawable extends Drawable {
    private Paint mPaint;
    private Paint.FontMetrics mFontMetrics;
    private String popStr = "";
    private Rect textBounds = new Rect();
    private int margin = 20;
    private Path mPath = new Path();

    private int height;
    private int popBgColor, popTextColor;
    private float textSize = 22;
    private int left, top;
    private int alpha = 255;
    private int width;

    public PopVerticalDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setPopBgColor(int popBgColor) {
        this.popBgColor = popBgColor;
    }

    public void setPopTextColor(int popTextColor) {
        this.popTextColor = popTextColor;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        mPaint.setTextSize(textSize);
        mFontMetrics = mPaint.getFontMetrics();
    }

    public void setHeight(int height) {
        //按设计图来  透明占据1/7
        this.height = (int) (height * 6.0f / 7);
    }

    public void setPopStr(String popStr) {
        this.popStr = popStr;
        this.mPaint.getTextBounds(popStr, 0, popStr.length(), textBounds);
        this.width = textBounds.width() + margin * 2;
    }

    public void drawVertical(Canvas canvas, int left, int mVerticalThumbCenterY) {
        this.left = left;
        this.top = mVerticalThumbCenterY - getIntrinsicHeight() / 2;
        draw(canvas);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!TextUtils.isEmpty(popStr)) {
            mPath.reset();
            mPath.addRoundRect(new RectF(0, 0, width, height)
                    , height / 2, height / 2, Path.Direction.CCW);
            mPaint.setColor(popBgColor);
            mPaint.setAlpha(alpha);
            canvas.translate(left - width - margin, top);
            canvas.drawPath(mPath, mPaint);

            mPaint.setColor(popTextColor);
            mPaint.setAlpha(alpha);
            float baselineY = height / 2 - (mFontMetrics.bottom - mFontMetrics.top) / 2 - mFontMetrics.top;
            canvas.drawText(popStr, margin, baselineY, mPaint);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}
