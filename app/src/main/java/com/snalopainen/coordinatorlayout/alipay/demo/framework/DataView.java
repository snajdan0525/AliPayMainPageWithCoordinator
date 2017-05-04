package com.snalopainen.coordinatorlayout.alipay.demo.framework;

/**
 * Created by snajdan on 16/9/12.
 */
public interface DataView<Data> {

    interface OnDataListener<Data> {
        void onDataLoaded(Data data);
        void onDataFailed();
    }

    void startLoad();

    Data getData();

    void setOnDataListener(OnDataListener<Data> listener);

}
