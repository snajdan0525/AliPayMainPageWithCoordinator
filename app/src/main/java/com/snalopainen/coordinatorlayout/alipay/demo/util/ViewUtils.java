package com.snalopainen.coordinatorlayout.alipay.demo.util;

/**
 * Created by snajdan on 2017/5/8.
 */


import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ViewUtils {

    private static <T extends View> ArrayList<T> findViewsByClass(ArrayList<T> viewList,
                                                                  Class<T> type, ViewGroup parent) {
        int len = parent.getChildCount();
        for (int i = 0; i < len; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                ViewUtils.findViewsByClass(viewList, type, (ViewGroup) child);
            } else if (type.isAssignableFrom(child.getClass())) {
                viewList.add((T) child);
            }
        }
        return viewList;
    }

    public static <T extends View> ArrayList<T> findViewsByClass(Class<T> type, ViewGroup parent) {
        return ViewUtils.findViewsByClass(new ArrayList<T>(), type, parent);
    }
}
