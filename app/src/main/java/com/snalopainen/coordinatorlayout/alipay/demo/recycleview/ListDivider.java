package com.snalopainen.coordinatorlayout.alipay.demo.recycleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.snalopainen.coordinatorlayout.alipay.demo.util.DensityUtil;

/**
 * This class is from the v7 samples of the Android SDK. It's not by me!
 * <p/>
 * See the license above for details.
 */
public class ListDivider extends RecyclerView.ItemDecoration {

    public static final float DEFAULT_DIVIDER_HEIGHT_DP = 8f;

    /* divider的高度，一般为1px或者0.5dp */
    private int dimension;

    private Paint colorPaint;

    private int mOrientation;

    private float paddingLeft, paddingRight, paddingTop, paddingBottom;

    public void setPadding(int left, int top, int right, int bottom) {
        paddingLeft = left;
        paddingTop = top;
        paddingRight = right;
        paddingBottom = bottom;
    }

    public ListDivider(Context context, int orientation, int color) {
        this.dimension = DensityUtil.dip2px(context, DEFAULT_DIVIDER_HEIGHT_DP);
        colorPaint = new Paint();
        colorPaint.setColor(color);
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + dimension;
            c.drawRect(left + paddingLeft, top + paddingTop, right - paddingRight, bottom - paddingBottom, colorPaint);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + dimension;
            c.drawRect(left + paddingLeft, top + paddingTop, right - paddingRight, bottom - paddingBottom, colorPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, dimension);
        } else {
            outRect.set(0, 0, dimension, 0);
        }
    }
}
