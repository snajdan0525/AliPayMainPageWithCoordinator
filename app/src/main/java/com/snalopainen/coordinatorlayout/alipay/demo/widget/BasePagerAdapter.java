package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by snajdan on 17/4/8.
 */

public abstract class BasePagerAdapter extends PagerAdapter {

    private SparseArray<View> pageCache = new SparseArray<>();

    /**
     * 当destroyItem时是否把view从viewpager中移除。默认true
     * false主要针对页面中有glsurfaceview的情况。
     */
    private boolean detachOnDestroyItem = true;

    protected abstract View getView(Context context, int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getCachedView(container.getContext(), position);
        if (view.getParent() == null) {
            container.addView(view, 0);
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (detachOnDestroyItem) {
            container.removeView((View) object);
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public View getCachedView(Context context, int position) {
        if (position > getCount() - 1) {
            return null;
        }
        View view = pageCache.get(position);
        if (view == null) {
            view = getView(context, position);
            pageCache.put(position, view);
        }
        return view;
    }

    public void setDetachOnDestroyItem(boolean detachOnDestroyItem) {
        this.detachOnDestroyItem = detachOnDestroyItem;
    }
}
