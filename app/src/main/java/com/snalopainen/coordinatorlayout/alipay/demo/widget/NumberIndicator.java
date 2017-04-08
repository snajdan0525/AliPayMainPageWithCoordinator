package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.snalopainen.coordinatorlayout.alipay.demo.util.DensityUtil;

/**
 * Created by snajdan on 16/9/23.
 */

public class NumberIndicator extends Indicator {

    private static final int DEFAULT_TEXT_COLOR = 0xaaffffff;

    private static final int DEFAULT_TEXT_SIZE = 14/*sp*/;

    private Paint paint;

    public NumberIndicator(Context context) {
        this(context, null);
    }

    public NumberIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(DEFAULT_TEXT_COLOR);
        paint.setFakeBoldText(true);
        paint.setTextSize(DensityUtil.sp2px(context, DEFAULT_TEXT_SIZE));
        paint.setAntiAlias(true);
    }

    public void setTextSize(int textSize) {
        paint.setTextSize(textSize);
        requestLayout();
    }

    public void setTextColor(int color) {
        paint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (paint.measureText(getMaxDisplay()) + .5f);
        int height = (int) (paint.getTextSize() + .5f);
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getCount();
        if (count <= 0) {
            return;
        }

        String text = getNumberDisplay();

        // 计算Baseline绘制的起点X轴坐标 ，计算方式：画布宽度的一半 - 文字宽度的一半
        int baseX = (int) (canvas.getWidth() / 2 - paint.measureText(text) / 2);

        // 计算Baseline绘制的Y坐标 ，计算方式：画布高度的一半 - 文字总高度的一半
        int baseY = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(getNumberDisplay(), baseX, baseY, paint);
    }

    private String getNumberDisplay() {
        return String.format("%d/%d", getPosition() + 1, getCount());
    }

    private String getMaxDisplay() {
        return String.format("%d/%d", getCount(), getCount());
    }
}
