package com.snalopainen.coordinatorlayout.alipay.demo.framework;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.boqii.android.framework.ui.R;
import com.boqii.android.framework.ui.recyclerview.RecyclerViewBaseAdapter;
import com.boqii.android.framework.ui.recyclerview.RecyclerViewUtil;
import com.boqii.android.framework.util.TaskUtil;

import java.util.ArrayList;

/**
 * Created by jinyan on 16/9/21.
 */

abstract class AdapterDataView<Data> extends SimpleDataView<ArrayList<Data>> {

    static interface LayoutType {
        final static int LIST = 0;
        final static int GRID = 1;
        final static int STAGGERED = 2;
        final static int H_LIST = 3;
        final static int H_GRID = 4;
        final static int H_STAGGERED = 5;
    }

    /**
     * default divider color
     */
    private static final int DIVIDER_COLOR = 0xffe5e5e5;

    protected RecyclerViewBaseAdapter<Data, ?> adapter;

    /**
     * see interface Layout
     */
    private int layoutType = LayoutType.LIST;

    /**
     * divider color
     */
    private int dividerColor = DIVIDER_COLOR;

    /**
     * columns or rows for grid/staggered
     */
    private int spanCount;

    protected abstract RecyclerViewBaseAdapter<Data, ?> createAdapter();

    protected abstract View createAdapterView(RecyclerViewBaseAdapter<Data, ?> adapter);

    protected abstract RecyclerView getRecyclerView(View contentView);

    public AdapterDataView(Context context) {
        this(context, null);
    }

    public AdapterDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ListDataView);
            layoutType = ta.getInt(R.styleable.ListDataView_layoutType, LayoutType.LIST);

            if (layoutType == LayoutType.GRID
                    || layoutType == LayoutType.H_GRID
                    || layoutType == LayoutType.STAGGERED
                    || layoutType == LayoutType.H_STAGGERED) {
                spanCount = ta.getInt(R.styleable.ListDataView_spanCount, 0);
                if (spanCount <= 1) {
                    throw new RuntimeException("grid/stagger's spanCount must > 1");
                }
            }

            ta.recycle();
        }

        adapter = createAdapter();
    }

    @Override
    public void clean() {
        adapter = createAdapter();
        super.clean();
    }

    @Override
    final protected View createView(Context context) {
        View view = createAdapterView(adapter);
        applyLayoutType(getRecyclerView(view), layoutType, spanCount, dividerColor);
        return view;
    }

    @Override
    final protected void bindView(View view, ArrayList<Data> datas) {
        adapter.dataSetAndNotify(datas);
    }

    @Override
    protected void onDataSuccess(final ArrayList<Data> data) {
        if (adapter.getHeaderCount() == 0 && (data == null || data.isEmpty())) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (contentView != null) {
                        removeView(contentView);
                        contentView = null;
                    }
                    loadingView.onEmpty();
                    ((View) loadingView).setVisibility(VISIBLE);
                }
            });
        } else {
            super.onDataSuccess(data);
        }
    }

    @Override
    public ArrayList<Data> getData() {
        return adapter.dataGetAll();
    }

    public void asList() {
        asList(DIVIDER_COLOR);
    }

    /**
     * color为0时 无分割线
     *
     * @param color
     */
    public void asList(int color) {
        layoutType = LayoutType.LIST;
        dividerColor = color;
        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, dividerColor);
        }
    }

    public void asHorizontalList() {
        asHorizontalList(DIVIDER_COLOR);
    }

    /**
     * color为0时 无分割线
     *
     * @param color
     */
    public void asHorizontalList(int color) {
        layoutType = LayoutType.H_LIST;

        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, color);
        }
    }

    public void asGrid(int columns) {
        layoutType = LayoutType.GRID;
        spanCount = columns;

        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, 0);
        }
    }

    public void asHorizontalGrid(int rows) {
        layoutType = LayoutType.H_GRID;
        spanCount = rows;

        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, 0);
        }
    }

    public void asStaggered(int columns) {
        layoutType = LayoutType.STAGGERED;
        spanCount = columns;

        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, 0);
        }
    }

    public void asHorizontalStaggered(int rows) {
        layoutType = LayoutType.H_STAGGERED;
        spanCount = rows;

        if (contentView != null) {
            applyLayoutType(getRecyclerView(contentView), layoutType, spanCount, 0);
        }
    }

    private void applyLayoutType(RecyclerView recyclerView, int layoutType, int spanCount, int color) {
        final Context context = recyclerView.getContext();
        switch (layoutType) {
            case LayoutType.LIST:
                RecyclerViewUtil.asList(recyclerView, color);
                break;
            case LayoutType.H_LIST:
                RecyclerViewUtil.asHorizontalList(recyclerView, color);
                break;
            case LayoutType.GRID:
                RecyclerViewUtil.asGrid(recyclerView, adapter.createGridLayoutManager(context, spanCount), spanCount);
                break;
            case LayoutType.H_GRID:
                RecyclerViewUtil.asHorizontalGrid(recyclerView, adapter.createGridLayoutManager(context, spanCount), spanCount);
                break;
            case LayoutType.STAGGERED:
                RecyclerViewUtil.asStaggered(recyclerView, spanCount);
                break;
            case LayoutType.H_STAGGERED:
                RecyclerViewUtil.asHorizontalStaggered(recyclerView, spanCount);
                break;
        }
    }
}
