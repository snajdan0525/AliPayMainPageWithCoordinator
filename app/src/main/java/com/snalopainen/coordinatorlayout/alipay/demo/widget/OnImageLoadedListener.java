package com.snalopainen.coordinatorlayout.alipay.demo.widget;

/**
 * Created by jinyan on 16/8/23.
 */
public interface OnImageLoadedListener {

    void onImageSet(int imageWidth, int imageHeight);

    void onImageFail(Throwable t);
}
