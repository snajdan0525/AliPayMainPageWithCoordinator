package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by snajdan on 17/4/8.
 */

public abstract class Indicator extends View {


    private int count;
    private int position;

    public Indicator(Context context) {
        super(context);
    }

    public Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCount(int count) {
        this.count = count;
        requestLayout();
        setVisibility(count > 1 ? VISIBLE : INVISIBLE);
    }

    public void setPosition(int position) {
        this.position = position;
        invalidate();
    }

    public int getCount() {
        return count;
    }

    public int getPosition() {
        return position;
    }
}
