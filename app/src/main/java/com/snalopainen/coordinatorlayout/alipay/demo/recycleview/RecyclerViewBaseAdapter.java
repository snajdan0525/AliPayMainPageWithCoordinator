package com.snalopainen.coordinatorlayout.alipay.demo.recycleview;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.snalopainen.coordinatorlayout.alipay.demo.R;
import com.snalopainen.coordinatorlayout.alipay.demo.widget.CheckableView;
import com.snalopainen.coordinatorlayout.alipay.demo.widget.UnClickableItem;

import java.util.ArrayList;

/**
 * Created by snajdan on 2017/30/3.
 */
public abstract class RecyclerViewBaseAdapter<Data, VH extends SimpleViewHolder> extends RecyclerView.Adapter<SimpleViewHolder> implements
        View.OnClickListener, View.OnLongClickListener {

    public static final int DATA_VIEW_TYPE_BASE = 0;
    private static final int HEADER_VIEW_TYPE_BASE = 100;
    private static final int FOOTER_VIEW_TYPE_BASE = 200;

    public static final int CHECK_MODE_NONE = 0;
    public static final int CHECK_MODE_SINGLE = 1;
    public static final int CHECK_MODE_MULTIPLE = 2;

    public static interface OnItemClickListener<Data> {
        void onItemClick(View view, Data data, int dataIndex);
    }

    public static interface OnItemLongClickListener<Data> {
        void onItemLongClick(View view, Data data, int dataIndex);
    }

    public static interface OnItemCheckListener<Data> {
        /**
         * 注意：单选模式下，取消选择时，dataIndex总是为-1。
         */
        void onItemCheck(int dataIndex, Data data, boolean checked);
    }

    public RecyclerViewBaseAdapter<Data, VH> setOnItemClickListener(OnItemClickListener<Data> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> setOnItemLongClickListener(OnItemLongClickListener<Data> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> setSingleCheckMode(OnItemCheckListener<Data> listener) {
        setCheckMode(CHECK_MODE_SINGLE, listener);
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> setMultipleCheckMode(OnItemCheckListener<Data> listener) {
        setCheckMode(CHECK_MODE_MULTIPLE, listener);
        return this;
    }

    public void disableCheckMode() {
        setCheckMode(CHECK_MODE_NONE, null);
    }

    private void setCheckMode(int mode, OnItemCheckListener<Data> listener) {
        this.checkMode = mode;
        this.onItemCheckListener = listener;
        if (checkMode == CHECK_MODE_NONE && dataCheckMap != null) {
            dataCheckMap.clear();
        }
        notifyDataSetChanged();
    }

    public RecyclerViewBaseAdapter<Data, VH> addHeaderView(View headerView) {
        if (headerViewList == null) {
            headerViewList = new ArrayList<>(5); /* 一般不会超过5个header view */
        }
        if (!headerViewList.contains(headerView)) {
            headerViewList.add(headerView);
            notifyDataSetChanged();
        }
        return this;
    }

    public void removeHeaderView(View headerView) {
        if (headerViewList != null && headerViewList.contains(headerView)) {
            headerViewList.remove(headerView);
            notifyDataSetChanged();
        }
    }

    public void clearHeaderView() {
        if (headerViewList != null) {
            headerViewList.clear();
            notifyDataSetChanged();
        }
    }

    public RecyclerViewBaseAdapter<Data, VH> addFooterView(View footerView) {
        if (footerViewList == null) {
            footerViewList = new ArrayList<>(2); /* 一般不会超过2个footer view */
        }
        if (!footerViewList.contains(footerView)) {
            footerViewList.add(footerView);
            notifyDataSetChanged();
        }
        return this;
    }

    public void removeFooterView(View footerView) {
        if (footerViewList != null && footerViewList.contains(footerView)) {
            footerViewList.remove(footerView);
            notifyDataSetChanged();
        }
    }

    public void clearFooterView() {
        if (footerViewList != null) {
            footerViewList.clear();
            notifyDataSetChanged();
        }
    }

    public int getHeaderCount() {
        return headerViewList == null ? 0 : headerViewList.size();
    }

    public int getFooterCount() {
        return footerViewList == null ? 0 : footerViewList.size();
    }

    public ArrayList<View> getHeaders() {
        return headerViewList;
    }

    public ArrayList<View> getFooters() {
        return footerViewList;
    }

    /******************
     * BEGIN: 处理数据变化
     *****************/

    public Data dataGet(int index) {
        return dataList.get(index);
    }

    public int dataGetIndex(Data data) {
        return dataList.indexOf(data);
    }

    public boolean dataExist(Data data) {
        return dataList != null && dataList.contains(data);
    }

    public ArrayList<Data> dataGetAll() {
        return dataList;
    }

    public Data dataGetFirst() {
        return dataGet(0);
    }

    public Data dataGetLast() {
        return dataGet(dataList.size() - 1);
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSet(ArrayList<Data> dataList) {
        this.dataList = dataList;
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetAndNotify(ArrayList<Data> dataList) {
        dataSet(dataList);
        notifyDataSetChanged();
        return this;
    }

    /* 使用场景一般为下拉加载更多 */
    public RecyclerViewBaseAdapter<Data, VH> dataAppend(ArrayList<Data> appendList) {
        if (dataList == null) {
            dataList = new ArrayList<>(appendList);
        } else {
            dataList.addAll(appendList);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataAppendAndNotify(ArrayList<Data> appendList) {
        dataAppend(appendList);
        /**
         * TODO 使用notifyXXX提升性能和动画效果
         */
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataAppend(Data data) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.add(data);
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataAppendAndNotify(Data data) {
        dataAppend(data);
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataInsert(int index, Data data) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.add(index, data);
        return this;
    }

    public void dataInsertAndNotify(int index, Data data) {
        dataInsert(index, data);
        /**
         * TODO 使用notifyXXX提升性能和动画效果
         */
        notifyDataSetChanged();
    }

    public RecyclerViewBaseAdapter<Data, VH> dataInsertAll(int index, ArrayList<Data> datas) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.addAll(index, datas);
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataInsertAllAndNotify(int index, ArrayList<Data> datas) {
        dataInsertAll(index, datas);
        notifyDataSetChanged();
        return this;
    }

    public Data dataRemove(int index) {
        return dataList.remove(index);
    }

    public Data dataRemoveAndNotify(int index) {
        Data data = dataRemove(index);
        /**
         * TODO 使用notifyXXX提升性能和动画效果
         */
        notifyDataSetChanged();
        return data;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataRemove(Data data) {
        if (dataList != null) {
            dataList.remove(data);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataRemoveAndNotify(Data data) {
        if (!dataExist(data)) {
            return this;
        }
        int position = dataGetIndex(data) + getHeaderCount();
        dataRemove(data);
        notifyItemRemoved(position);
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataUpdate(Data data) {
        if (dataList != null) {
            int size = dataList.size();
            int index = -1;
            for (int i = 0; i < size; i++) {
                if (data.equals(dataList.get(i))) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                dataList.set(index, data);
            }
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataUpdateAndNotify(Data data) {
        dataUpdate(data);
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataClear() {
        if (dataList != null) {
            dataList.clear();
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataClearAndNotify() {
        dataClear();
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetChecked(Data data, boolean checked) {
        if (dataCheckMap == null) {
            dataCheckMap = new ArrayMap<>();
        }
        if (checked) {
            dataCheckMap.put(data, Boolean.TRUE);
        } else {
            dataCheckMap.remove(data);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetAllUnChecked() {
        dataCheckMap = null;
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetDisabled(Data data, boolean disabled) {
        if (dataDisableMap == null) {
            dataDisableMap = new ArrayMap<>();
        }
        if (disabled) {
            dataDisableMap.put(data, Boolean.TRUE);
        } else {
            dataDisableMap.remove(data);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetCheckAndNotify(Data data, boolean checked) {
        dataSetChecked(data, checked);
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataSetDisabledAndNotify(Data data, boolean disabled) {
        dataSetDisabled(data, disabled);
        notifyDataSetChanged();
        return this;
    }

    private boolean dataGetChecked(Data data) {
        return dataCheckMap == null ? false : dataCheckMap.get(data) == Boolean.TRUE;
    }

    private boolean dataGetDisabled(Data data) {
        return dataDisableMap == null ? false : dataDisableMap.get(data) == Boolean.TRUE;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataCheckAll() {
        int size = getDataCount();
        for (int i = 0; i < size; i++) {
            dataSetCheckAndNotify(dataGet(i), true);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataCheckAllAndNotify() {
        dataCheckAll();
        notifyDataSetChanged();
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataUnCheckAll() {
        int size = getDataCount();
        for (int i = 0; i < size; i++) {
            dataSetCheckAndNotify(dataGet(i), false);
        }
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> dataUnCheckAllAndNotify() {
        dataUnCheckAll();
        notifyDataSetChanged();
        return this;
    }

    public boolean dataAllChecked() {
        int size = getDataCount();
        for (int i = 0; i < size; i++) {
            if (!dataGetChecked(dataGet(i))) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Data> dataGetChecked() {
        if (dataCheckMap == null) {
            return null;
        }
        return new ArrayList<>(dataCheckMap.keySet());
    }

    /****************** END: 处理数据变化 *****************/

    /******************
     * BEGIN: RecyclerView.Adapter 实现部分
     *****************/

    @Override
    public int getItemCount() {
        if (hideHeadIfNoData && isDataEmpty()) {
            return 0;
        } else {
            return getDataCount() + getHeaderViewCount() + getFooterViewCount();
        }
    }

    public boolean isDataEmpty() {
        return dataList == null || getDataCount() == 0;
    }

    @Override
    final public int getItemViewType(int position) {
        int headerViewCount = getHeaderViewCount();
        if (position < headerViewCount) {
            return position + HEADER_VIEW_TYPE_BASE;
        }
        int dataCount = getDataCount();
        if (position < headerViewCount + dataCount) {
            return getDataItemViewType(position - headerViewCount);
        }
        return position - headerViewCount - dataCount + FOOTER_VIEW_TYPE_BASE;
    }

    /**
     * index is from 0 to dataCount - 1
     */
    protected int getDataItemViewType(int dataIndex) {
        return DATA_VIEW_TYPE_BASE;
    }

    @Override
    final public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == FOOTER_VIEW_TYPE_BASE) {
//            View footerView = footerViewList.get(viewType - FOOTER_VIEW_TYPE_BASE);
//            if (footerView.getLayoutParams() == null) {
//                footerView.setLayoutParams(createDefaultLayoutParam());
//            }
//            return new SimpleViewHolder(footerView);
//        }
        if (viewType== HEADER_VIEW_TYPE_BASE) {
            View headerView = headerViewList.get(viewType - HEADER_VIEW_TYPE_BASE);
            if (headerView.getLayoutParams() == null) {
                headerView.setLayoutParams(createDefaultLayoutParam());
            }
            return new SimpleViewHolder(headerView);
        }
        VH vh = onCreateDataViewHolder(parent, viewType);
        if (vh.itemView.getLayoutParams() == null) {
            vh.itemView.setLayoutParams(createDefaultLayoutParam());
        }
        return vh;
    }

    protected abstract VH onCreateDataViewHolder(ViewGroup parent, int viewType);

    /**
     * 子类不用实现onBindViewHolder，现在需要实现onRealBindViewHolder。做的事情一样
     *
     * @param holder
     * @param dataIndex
     */
    protected abstract void onBindDataViewHolder(VH holder, Data data, int dataIndex);

    /**
     * 子类不要重现此方法，请重写onRealBindViewHolder
     */
    @Override
    final public void onBindViewHolder(SimpleViewHolder holder, int position) {
        final int headerViewCount = getHeaderViewCount();
        final int dataCount = getDataCount();
        /* only bind data view holder */
        if (headerViewCount <= position && position < headerViewCount + dataCount) {
            final int dataIndex = position - headerViewCount;
            onBindDataViewHolder((VH) holder, dataGet(dataIndex), dataIndex);

            if (holder.itemView instanceof CheckableView) {
                CheckableView checkableView = (CheckableView) holder.itemView;
                checkableView.showCheckBox(checkMode != CHECK_MODE_NONE);
                if (checkMode != CHECK_MODE_NONE) {
                    checkableView.setChecked(dataGetChecked(dataGet(dataIndex)));
                    checkableView.setDisabled(dataGetDisabled(dataGet(dataIndex)));
                }
            }

            boolean setBg = false;
            if (onItemClickListener != null || onItemCheckListener != null) {
                if (holder.itemView instanceof UnClickableItem) {
                    setBg = false;
                } else {
                    setBg = true;
                }
                if (setBg) {
                    holder.itemView.setTag(R.id.id_data_index, Integer.valueOf(dataIndex));
                    holder.itemView.setOnClickListener(this);
                }
            }
            if (onItemLongClickListener != null) {
                setBg = true;
                holder.itemView.setTag(R.id.id_data_index, Integer.valueOf(dataIndex));
                holder.itemView.setOnLongClickListener(this);
            }
            if (setBg && itemBgSelector != 0) {
                holder.itemView.setBackgroundResource(itemBgSelector);
            }
        }

        if (rotation) {
            holder.itemView.setRotation(180);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof CheckableView) {
            CheckableView item = (CheckableView) v;
            if (item.isItemNoClickble()) {
                return;
            }
        }
        int dataIndex = ((Integer) v.getTag(R.id.id_data_index)).intValue();
        Data data = dataGet(dataIndex);
        if (checkMode != CHECK_MODE_NONE && v instanceof CheckableView) {
            if (!dataGetDisabled(data)) {
                CheckableView item = (CheckableView) v;
                boolean target = !item.isChecked();
                if (checkMode == CHECK_MODE_MULTIPLE) {
                    item.setChecked(target);
                    dataSetChecked(data, target);
                    onItemCheckListener.onItemCheck(dataIndex, data, target);
                } else if (checkMode == CHECK_MODE_SINGLE) {
                    /* 单选模式，无法取消选择 */
                    if (target == true/* 目标为选择 */) {
                        /* 首先取消当前的选择 */
                        ArrayList<Data> currentCheckedList = dataGetChecked();/* 理论上为0或1个 */
                        if (currentCheckedList != null) {
                            int size = currentCheckedList.size();
                            for (int i = 0; i < size; i++) {
                                dataSetChecked(currentCheckedList.get(i), false);
                                onItemCheckListener.onItemCheck(-1, currentCheckedList.get(i), false);
                            }
                        }
                        /* 选择当前点击项 */
                        dataSetChecked(data, true);
                        notifyDataSetChanged();
                        onItemCheckListener.onItemCheck(dataIndex, data, target);

                    }
                }
            }
        } else {
            onItemClickListener.onItemClick(v, data, dataIndex);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int dataIndex = ((Integer) v.getTag(R.id.id_data_index)).intValue();
        Data data = dataGet(dataIndex);
        onItemLongClickListener.onItemLongClick(v, data, dataIndex);
        return true;
    }

    private static RecyclerView.LayoutParams createDefaultLayoutParam() {
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return lp;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridSpanSizeLookup(gridManager.getSpanCount()));
        }
    }

    private class GridSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        private int spanCount;

        public GridSpanSizeLookup(int spanCount) {
            this.spanCount = spanCount;
        }

        @Override
        public int getSpanSize(int position) {
            int viewType = getItemViewType(position);
            if (viewType >= HEADER_VIEW_TYPE_BASE || viewType >= FOOTER_VIEW_TYPE_BASE) {
                return spanCount;
            }
            return 1;
        }
    }

    public GridLayoutManager createGridLayoutManager(Context context, int spanCount) {
        GridLayoutManager manager = new GridLayoutManager(context, spanCount);
        manager.setSpanSizeLookup(new GridSpanSizeLookup(spanCount));
        return manager;
    }

    @Override
    public void onViewAttachedToWindow(SimpleViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;

            int viewType = getItemViewType(holder.getLayoutPosition());
            p.setFullSpan(viewType >= HEADER_VIEW_TYPE_BASE || viewType >= FOOTER_VIEW_TYPE_BASE);
        }
    }

    /******************
     * END: RecyclerView.Adapter 实现部分
     *****************/

    private int getHeaderViewCount() {
        return headerViewList != null ? headerViewList.size() : 0;
    }

    private int getFooterViewCount() {
        return footerViewList != null ? footerViewList.size() : 0;
    }

    public int getDataCount() {
        return dataList != null ? dataList.size() : 0;
    }

    /**
     * 当没有数据时是否显示header
     *
     * @param hide
     */
    public RecyclerViewBaseAdapter<Data, VH> setHideHeadIfNoData(boolean hide) {
        hideHeadIfNoData = hide;
        return this;
    }

    public RecyclerViewBaseAdapter<Data, VH> setItemBgSelector(int itemBgSelector) {
        this.itemBgSelector = itemBgSelector;
        return this;
    }

    private ArrayList<Data> dataList;

    private ArrayMap<Data, Boolean> dataCheckMap;

    private ArrayMap<Data, Boolean> dataDisableMap;

    private OnItemClickListener onItemClickListener;

    private OnItemLongClickListener onItemLongClickListener;

    private OnItemCheckListener<Data> onItemCheckListener;

    private ArrayList<View> headerViewList;

    private ArrayList<View> footerViewList;

    private boolean hideHeadIfNoData = false;

    private int checkMode = CHECK_MODE_NONE;

    private int itemBgSelector = R.drawable.ui_bg_selector;

    /* 用于类似微信公众号，将ptrlist整个翻转。 */
    private boolean rotation;

    public void setRotation(boolean rotation) {
        this.rotation = rotation;
    }
}
