package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;

/**
 * 用于抽象图片框架
 * Created by JinYan on 2016/7/29.
 */
interface ImageImp {

    void initialize(Context context, Object initParam);

    void loadBitmap(String uri, BqImageCallback callback);

    void loadBitmap(String uri, int width, int height, BqImageCallback callback);

    void clearCache();

    long getCacheSize();
}
