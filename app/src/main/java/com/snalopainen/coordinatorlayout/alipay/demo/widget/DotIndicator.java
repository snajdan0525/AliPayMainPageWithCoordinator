package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.boqii.android.framework.util.DensityUtil;
import com.snalopainen.coordinatorlayout.alipay.demo.util.DensityUtil;

/**
 * Created by snajdan on 17/4/8.
 */

public class DotIndicator extends Indicator {

    private static final int DOT_RADIUS = 3;
    private static final int DOT_GAP = 4;

    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xFFffffff;
    private static final int DEFAULT_NORMAL_COLOR = 0x7fffffff;

    private int dotRadius;
    private int dotGap;

    private int normalColor;
    private int highlightColor;

    private Paint paint;

    public DotIndicator(Context context) {
        this(context, null);
    }

    public DotIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        dotRadius = DensityUtil.dip2px(context, DOT_RADIUS);
        dotGap = DensityUtil.dip2px(context, DOT_GAP);

        normalColor = DEFAULT_NORMAL_COLOR;
        highlightColor = DEFAULT_HIGHLIGHT_COLOR;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setColors(int normalColor, int highlightColor) {
        this.normalColor = normalColor;
        this.highlightColor = highlightColor;
        invalidate();
    }

    public void setDotRadius(int radius) {
        dotRadius = radius;
        invalidate();
    }

    public void setDotGap(int gap) {
        dotGap = gap;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getCount();
        int width = count == 0 ? 0 : dotRadius * 2 * count + dotGap * (count - 1);
        int height = count == 0 ? 0 : dotRadius * 2;
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getCount();
        int cx, cy = dotRadius;
        for (int i = 0; i < count; i++) {
            cx = dotRadius + i * (dotRadius + dotRadius + dotGap);
            paint.setColor(i == getPosition() ? highlightColor : normalColor);
            canvas.drawCircle(cx, cy, dotRadius, paint);
        }
    }
}
