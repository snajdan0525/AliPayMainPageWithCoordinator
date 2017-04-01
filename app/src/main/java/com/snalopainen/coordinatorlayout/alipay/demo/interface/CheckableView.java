package com.snalopainen.coordinatorlayout.alipay.demo.widget;

/**
 * Created by snajdan on 2017/30/3.
 */
public interface CheckableView {

    void showCheckBox(boolean show);

    void setChecked(boolean checked);

    boolean isChecked();

    /** 设置点击是否改变check状态 */
    void setDisabled(boolean disabled);
    
    /** 设置是否不可以点击 ,默认为false*/
    boolean isItemNoClickble();
}
