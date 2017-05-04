package com.snalopainen.coordinatorlayout.alipay.demo.framework;

import com.boqii.android.framework.data.DataMiner;

/**
 * Created by snajdan on 16/9/9.
 */
public interface LoadingView {

    void onLoading();

    void onError(DataMiner.DataMinerError error, DataRetryHandler dataRetryHandler);

    void onEmpty();

}
