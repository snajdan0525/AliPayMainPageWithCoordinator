package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * Author: snajdan
 */
public class sNajdanViewPager extends ViewPager {


    /**
     * 当切换到一个新到page时,延迟SETUP_DELAY后setup view。
     * 这个delay时间越久越流畅，但是太长用户需要等
     */
    private static final int SETUP_DELAY = 300;

    public sNajdanViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        listerWrapper = new OnPageChangeListenerWrapper();
        setOnPageChangeListener(listerWrapper);
    }

    public sNajdanViewPager(Context context) {
        super(context);
        listerWrapper = new OnPageChangeListenerWrapper();
        setOnPageChangeListener(listerWrapper);
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        if (listener instanceof OnPageChangeListenerWrapper) {
            super.setOnPageChangeListener(listener);
        } else {
            listerWrapper.target = listener;
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mInitStatus = new SparseArray<>();
        super.setAdapter(adapter);
    }

    public void showPage(final int page) {
        targetPosition = page;

        if (getCurrentItem() == page) {

            post(new Runnable() {
                @Override
                public void run() {
                    if (getAdapter() instanceof BasePagerAdapter) {
                        BasePagerAdapter basePagerAdapter = (BasePagerAdapter) getAdapter();
                        Page viewPagerItem = null;
                        {
                            View view = basePagerAdapter.getCachedView(getContext(), page);
                            if (view instanceof Page) {
                                viewPagerItem = (Page) view;
                            }
                        }

                        if (currentItem != null && currentItem != viewPagerItem) {
                            currentItem.onPageHide();
                            currentItem = null;
                        }

                        if (viewPagerItem != null) {
                            if (mInitStatus.get(page) != Boolean.TRUE) {
                                mInitStatus.put(page, Boolean.TRUE);
                                viewPagerItem.onPageInit();
                                viewPagerItem.onPageShow();
                            }

                            currentItem = viewPagerItem;
                        }
                    }
                }
            });

        } else {
            setCurrentItem(page);
        }
    }

    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isScrollable && super.onTouchEvent(ev);
    }


    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, smoothScroll);
    }

    public void setSmoothScroll(boolean smoothScroll) {
        this.smoothScroll = smoothScroll;
    }

    private SparseArray<Boolean> mInitStatus;

    private boolean isScrollable = true;

    private Runnable delaySetupRunnable;

    private class OnPageChangeListenerWrapper implements OnPageChangeListener {

        private OnPageChangeListener target;

        @Override
        public void onPageScrollStateChanged(int state) {
            if (target != null) {
                target.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (target != null) {
                target.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            targetPosition = position;
            if (target != null) {
                target.onPageSelected(position);
            }

            if (getAdapter() instanceof BasePagerAdapter) {
                BasePagerAdapter basePagerAdapter = (BasePagerAdapter) getAdapter();

                final Page viewPagerItem;
                {
                    View view = basePagerAdapter.getCachedView(getContext(), position);
                    if (view instanceof Page) {
                        viewPagerItem = (Page) view;
                    } else {
                        viewPagerItem = null;
                    }
                }

                if (currentItem != null && currentItem != viewPagerItem) {
                    currentItem.onPageHide();
                    currentItem = null;
                }

                if (viewPagerItem != null) {
                    if (mInitStatus.get(position) != Boolean.TRUE) {
                        if (delaySetupRunnable != null) {
                            removeCallbacks(delaySetupRunnable);
                        }
                        delaySetupRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (targetPosition == position) {
                                    mInitStatus.put(position, Boolean.TRUE);
                                    viewPagerItem.onPageInit();
                                    viewPagerItem.onPageShow();
                                    currentItem = viewPagerItem;
                                    delaySetupRunnable = null;
                                }
                            }
                        };
                        postDelayed(delaySetupRunnable, SETUP_DELAY);
                    } else {
                        viewPagerItem.onPageShow();
                        currentItem = viewPagerItem;
                    }
                }
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMaxHeight(widthMeasureSpec), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getChildMaxHeight(int widthMeasureSpec) {
        if (getAdapter() instanceof BasePagerAdapter) {
            BasePagerAdapter adapter = (BasePagerAdapter) getAdapter();
            View currentView = adapter.getCachedView(getContext(), getCurrentItem());
            if (currentView != null) {
                currentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                return currentView.getMeasuredHeight();
            }
        }

        int size = getChildCount();
        int maxHeight = 1;
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            if (child.getMeasuredHeight() > maxHeight) {
                maxHeight = child.getMeasuredHeight();
            }
        }
        return maxHeight;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        showPage(getCurrentItem());
    }

    private OnPageChangeListenerWrapper listerWrapper;

    private Page currentItem;

    private int targetPosition;

    private boolean smoothScroll = true;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollable && super.onInterceptTouchEvent(ev);
    }
}
