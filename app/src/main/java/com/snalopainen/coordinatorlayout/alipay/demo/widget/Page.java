package com.snalopainen.coordinatorlayout.alipay.demo.widget;

/**
 * BasePagerAdapter getView()获得的自定义view可以实现Page接口,以获得状态回调。
 * 状态: onPageInit() -> onPageShow() -> onPageHide() -> onPageShow() -> onPageHide() ...
 * <p>
 * Created by snajdan on 2017/4/8.
 */
public interface Page {

    /**
     * 子view第一次显示时调用,且只会调用一次。在这里可以加载数据，从而达到了lazy load的效果。
     */
    void onPageInit();

    void onPageShow();

    void onPageHide();
}