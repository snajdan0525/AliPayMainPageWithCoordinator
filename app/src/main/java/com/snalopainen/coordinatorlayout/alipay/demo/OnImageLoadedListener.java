package com.snalopainen.coordinatorlayout.alipay.demo;

/**
 * Created by jinyan on 16/8/23.
 */
public interface OnImageLoadedListener {

    void onImageSet(int imageWidth, int imageHeight);

    void onImageFail(Throwable t);
}
