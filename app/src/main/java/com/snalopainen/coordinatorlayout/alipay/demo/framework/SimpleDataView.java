package com.snalopainen.coordinatorlayout.alipay.demo.framework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.boqii.android.framework.data.DataMiner;
import com.boqii.android.framework.data.entity.BaseDataEntity;
import com.boqii.android.framework.util.StringUtil;
import com.boqii.android.framework.util.TaskUtil;

/**
 * Created by jinyan on 16/9/9.
 */
public abstract class SimpleDataView<Data> extends RelativeLayout implements DataView<Data>, DataMiner.DataMinerObserver {

    protected static final int INIT_MINER_ID = 1;

    protected LoadingView loadingView;

    protected View contentView;

    protected OnDataListener onDataListener;

    protected Data data;

    public static interface LoadingViewProvider {
        LoadingView createLoadingView(Context context);
    }

    static LoadingViewProvider loadingViewProvider = new LoadingViewProvider() {
        @Override
        public LoadingView createLoadingView(Context context) {
            return new DefaultLoadingView(context, null);
        }
    };

    public static void setLoadingViewProvider(LoadingViewProvider provider) {
        loadingViewProvider = provider;
    }

    public SimpleDataView(Context context) {
        this(context, null);
    }

    public SimpleDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadingView = createLoadingView(context);
        loadingView.onLoading();
        addView((View) loadingView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected abstract DataMiner createDataMiner(DataMiner.DataMinerObserver observer);

    protected abstract View createView(Context context);

    protected abstract void bindView(View view, Data data);

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setOnDataListener(OnDataListener<Data> listener) {
        onDataListener = listener;
    }

    protected LoadingView createLoadingView(Context context) {
        return loadingViewProvider.createLoadingView(context);
    }

    public void startLoad() {
        startLoad(false);
    }

    public void clean() {
        if (contentView != null) {
            removeView(contentView);
            contentView = null;
        }
    }

    public void cleanAndReload() {
        clean();
        startLoad(true);
    }

    private void startLoad(boolean forceRefresh) {
        DataMiner miner = createDataMiner(this);
        miner.setId(INIT_MINER_ID).work(forceRefresh ? DataMiner.FetchType.FailThenStale : DataMiner.FetchType.Normal);
        ((View) loadingView).setVisibility(VISIBLE);
        loadingView.onLoading();
    }

    /**
     * 已经加载过数据后，希望刷新数据（由于某种情况引起服务器端数据以改变）。refresh完全无视缓存，强制请求服务器。
     */
    public void refresh() {
        refreshWithLoadingMessage(null);
    }

    public void refreshWithLoadingMessage() {
        refreshWithLoadingMessage("正在刷新列表");
    }

    public void refreshWithLoadingMessage(String msg) {
        DataMiner miner = createDataMiner(this);
        if (StringUtil.isNotBlank(msg)) {
            miner.showLoading(getContext(), msg);
        }
        miner.setId(INIT_MINER_ID).work(DataMiner.FetchType.OnlyRemote);
    }


    @Override
    public boolean onDataError(DataMiner miner, final DataMiner.DataMinerError error) {
        if (onDataListener != null) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDataListener.onDataFailed();
                }
            });
        }
        if (contentView != null) {
            return false;
        }
        TaskUtil.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                DataRetryHandler handler = error.getType() != DataMiner.DataMinerError.ERROR_TYPE_BUSINESS ?
                        new DataRetryHandler() {
                            @Override
                            public void doDataRetry() {
                                startLoad();
                            }
                        } : null;

                loadingView.onError(error, handler);
            }
        });
        return true;
    }

    protected Data getDataFromMiner(DataMiner miner) {
        if (miner.getData() instanceof BaseDataEntity) {
            BaseDataEntity<Data> entity = miner.getData();
            return entity.getResponseData();
        } else {
            return miner.getData();
        }
    }

    @Override
    public void onDataSuccess(final DataMiner miner) {
        data = getDataFromMiner(miner);

        onDataSuccess(data);

        if (onDataListener != null) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDataListener.onDataLoaded(data);
                }
            });
        }
    }

    protected void onDataSuccess(final Data data) {
        TaskUtil.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contentView == null) {
                    contentView = createView(getContext());
                    if (contentView.getLayoutParams() == null) {
                        addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    } else {
                        addView(contentView);
                    }
                }
                bindView(contentView, data);
                ((View) loadingView).setVisibility(GONE);
            }
        });
    }

}
