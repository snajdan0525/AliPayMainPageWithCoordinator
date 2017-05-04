package com.snalopainen.coordinatorlayout.alipay.demo.framework;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.boqii.android.framework.data.entity.BaseDataEntity;
import com.boqii.android.framework.data.entity.BaseMetaDataEntity;
import com.boqii.android.framework.data.entity.PageMetaData;
import com.boqii.android.framework.ui.R;
import com.boqii.android.framework.ui.recyclerview.RecyclerViewBaseAdapter;
import com.boqii.android.framework.util.ListUtil;
import com.boqii.android.framework.util.TaskUtil;

import java.util.ArrayList;

/**
 * Created by jinyan on 16/9/9.
 */
public abstract class PTRListDataView<Data> extends AdapterDataView<Data> implements PTRRecyclerView.PullToRefreshHandler {

    private static final int REFRESH_MINER_ID = INIT_MINER_ID + 1;
    private static final int LOAD_MORE_MINER_ID = INIT_MINER_ID + 2;

    private OnRefreshListener onRefreshListener;

    private PageMetaData pageMetaData;

    private boolean canRefresh = true;
    private boolean canLoadMore = true;
    private boolean rotation = false;

    public PTRListDataView(Context context) {
        super(context);
    }

    public PTRListDataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PTRListDataView);
            canRefresh = ta.getBoolean(R.styleable.PTRListDataView_canRefresh, canRefresh);
            canLoadMore = ta.getBoolean(R.styleable.PTRListDataView_canLoadMore, canLoadMore);
            ta.recycle();
        }
    }

    public void setRotation(boolean rotation) {
        this.rotation = rotation;
    }

    final protected DataMiner createDataMiner(DataMiner.DataMinerObserver observer) {
        return createRefreshDataMiner(observer);
    }

    protected abstract DataMiner createRefreshDataMiner(DataMiner.DataMinerObserver observer);

    protected abstract DataMiner createLoadMoreDataMiner(DataMiner.DataMinerObserver observer);

    protected boolean hasMoreData(ArrayList<Data> data) {
        return data != null && !data.isEmpty();
    }

    @Override
    protected View createAdapterView(RecyclerViewBaseAdapter<Data, ?> adapter) {
        PTRRecyclerView ptrRecyclerView = new PTRRecyclerView(getContext());
        ptrRecyclerView.setPullToRefreshHandler(PTRListDataView.this);
        ptrRecyclerView.setAdapter(adapter);
        ptrRecyclerView.setHasMore(false);

        ptrRecyclerView.setCanRefresh(canRefresh);
        ptrRecyclerView.setCanLoadMore(false);

        if (rotation) {
            adapter.setRotation(true);
            ptrRecyclerView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
            ptrRecyclerView.setRotation(180);
        }

        return ptrRecyclerView;
    }

    public RecyclerView getRecyclerView() {
        return contentView == null ? null : ((PTRRecyclerView) contentView).getRecyclerView();
    }

    @Override
    protected RecyclerView getRecyclerView(View contentView) {
        return ((PTRRecyclerView) contentView).getRecyclerView();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
        if (contentView != null) {
            ((PTRRecyclerView) contentView).setCanLoadMore(canLoadMore);
        }
    }

    public void setCanRefresh(boolean canRefresh) {
        this.canRefresh = canRefresh;
        if (contentView != null) {
            ((PTRRecyclerView) contentView).setCanRefresh(canRefresh);
        }
    }

    public PageMetaData getPageMetaData() {
        return pageMetaData;
    }

    protected PageMetaData getPageMetaDataFromMiner(DataMiner miner) {
        BaseDataEntity<?> entity = miner.getData();
        if (entity instanceof BaseMetaDataEntity) {
            return (PageMetaData) ((BaseMetaDataEntity) entity).getMetadata();
        }
        return null;
    }

    @Override
    public void onDataSuccess(DataMiner miner) {

        pageMetaData = getPageMetaDataFromMiner(miner);

        final int minerId = miner.getId();
        if (minerId == REFRESH_MINER_ID || minerId == INIT_MINER_ID) {
            super.onDataSuccess(miner);
            if (data != null && !data.isEmpty()) {
                TaskUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (contentView != null) {
                            PTRRecyclerView ptrRecyclerView = (PTRRecyclerView) contentView;
                            ptrRecyclerView.setCanLoadMore(canLoadMore);
                            ptrRecyclerView.setHasMore(hasMoreData(data));
                        }
                    }
                });
            }

            if (minerId == REFRESH_MINER_ID) {
                TaskUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (contentView != null) {
                            PTRRecyclerView ptrRecyclerView = (PTRRecyclerView) contentView;
                            ptrRecyclerView.setRefreshFinished();
                        }
                    }
                });
            }

        } else if (minerId == LOAD_MORE_MINER_ID) {

            final ArrayList<Data> data = getDataFromMiner(miner);
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PTRRecyclerView ptrRecyclerView = (PTRRecyclerView) contentView;
                    if (ptrRecyclerView != null && !ptrRecyclerView.isLoadingMoreCancelled()) {
                        ptrRecyclerView.setLoadMoreFinished();

                        if (ListUtil.isNotEmpty(data)) {
                            adapter.dataAppendAndNotify(data);
                        }

                        ptrRecyclerView.setHasMore(hasMoreData(data));
                    }
                }
            });
        }

    }

    @Override
    public boolean onDataError(DataMiner miner, DataMiner.DataMinerError error) {
        final int minerId = miner.getId();
        if (INIT_MINER_ID == minerId || REFRESH_MINER_ID == minerId) {
            if (REFRESH_MINER_ID == minerId) {
                TaskUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PTRRecyclerView ptrRecyclerView = (PTRRecyclerView) contentView;
                        if (ptrRecyclerView != null) {
                            ptrRecyclerView.setRefreshFinished();
                        }
                    }
                });
            }
            return super.onDataError(miner, error);
        } else if (LOAD_MORE_MINER_ID == minerId) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PTRRecyclerView ptrRecyclerView = (PTRRecyclerView) contentView;
                    if (ptrRecyclerView != null && !ptrRecyclerView.isLoadingMoreCancelled()) {
                        ptrRecyclerView.setLoadMoreError();
                    }
                }
            });
            return true;
        }

        return false;
    }

    @Override
    public void doLoadMore() {
        if (null != onRefreshListener) {
            onRefreshListener.onPullUpToRefresh(this);
        }
        DataMiner miner = createLoadMoreDataMiner(this);
        miner.setId(LOAD_MORE_MINER_ID).work(DataMiner.FetchType.FailThenStale);
    }

    @Override
    public void doDataRefresh() {
        if (null != onRefreshListener) {
            onRefreshListener.onPullDownToRefresh(this);
        }
        pageMetaData = null;
        DataMiner miner = createRefreshDataMiner(this);
        miner.setId(REFRESH_MINER_ID).work(DataMiner.FetchType.OnlyRemote);
    }
}
