package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snalopainen.coordinatorlayout.alipay.demo.R;
import com.snalopainen.coordinatorlayout.alipay.demo.recycleview.Bindable;
import com.snalopainen.coordinatorlayout.alipay.demo.util.DensityUtil;

import java.util.ArrayList;

/**
 * Created by snajdan on 2017/30/3.
 */

public abstract class NavigationView<T> extends LinearLayout implements Bindable<ArrayList<T>>, View.OnClickListener {

    private int maxColumn = Integer.MAX_VALUE;

    protected abstract int getNavigationItemImage(T data);

    protected abstract String getNavigationItemTitle(T data);

    protected abstract void onNavigationItemClick(T data, int index);

    private Context context;
    int ROW;
    int COLUMN;

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        int padding = DensityUtil.dip2px(context, 4);
        setPadding(padding, padding, padding, padding);
    }

    public void setMaxColumn(int column) {
        maxColumn = column;
    }

    @Override
    public void bind(ArrayList<T> data) {
        removeAllViews();
        if (data == null) {
            return;
        }

        final int size = data.size();
        ROW = size / maxColumn + (size % maxColumn == 0 ? 0 : 1);
        COLUMN = ROW == 1 ? size : maxColumn;

        for (int row = 0; row < ROW; row++) {
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(HORIZONTAL);
            rowLayout.setWeightSum(COLUMN);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(rowLayout, lp);
            for (int column = 0; column < COLUMN; column++) {
                int index = row * COLUMN + column;
                if (index >= size) {
                    break;
                }
                rowLayout.addView(createNavigationItem(data.get(index)));
            }
        }
    }

    private int getClickedItemIndex(View v) {
        int index = -1;
        ViewGroup vRow;
        View vItem;
        for (int i = 0; i < ROW; i++) {
            vRow = (ViewGroup) getChildAt(i);
            for (int j = 0; j < COLUMN; j++) {
                vItem = vRow.getChildAt(j);
                if (vItem == v) {
                    index = i * COLUMN + j;
                    return index;
                }
            }
        }
        return index;
    }

    @Override
    public void onClick(View v) {
        T data = (T) v.getTag();
        int index = getClickedItemIndex(v);
        if (index == -1)
            return;
        onNavigationItemClick(data, index);
    }

    private View createNavigationItem(T data) {
        View view = View.inflate(getContext(), R.layout.navigation_item, null);
        ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(getNavigationItemImage(data));
        ((TextView) view.findViewById(android.R.id.title)).setText(getNavigationItemTitle(data));
        LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        view.setLayoutParams(lp);

        view.setOnClickListener(this);
        view.setTag(data);

        return view;
    }

    public int calculateHeight(int rows) {
        View view = View.inflate(getContext(), R.layout.navigation_item, null);
        int ms = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(ms, ms);
        return view.getMeasuredHeight() * rows + getPaddingTop() + getPaddingBottom();
    }

}
