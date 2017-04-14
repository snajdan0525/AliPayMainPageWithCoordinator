package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.snalopainen.coordinatorlayout.alipay.demo.R;
import com.snalopainen.coordinatorlayout.alipay.demo.util.DensityUtil;

import java.lang.ref.WeakReference;

/**
 * Created by jinyan on 16/9/22.
 */

public class Slider extends RelativeLayout implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = Slider.class.getSimpleName();

    public static final long INTERVAL = 5000;

    public static final int INDICATOR_TYPE_DOT = 0;
    public static final int INDICATOR_TYPE_NUMBER = 1;

    public static interface OnSlideClickListener {
        public void onSlideClick(Slider slider, int position);
    }

    /**
     * 类似ListView的adapter
     */
    public static interface SliderProvider {
        int getCount();

        View getView(Context context, int position, View convertView);
    }

    private sNajdanViewPager viewPager;
    private SliderProvider sliderProvider;
    private Adapter adapter;
    private Handler handler;

    private int indicatorType = INDICATOR_TYPE_DOT;
    private Indicator indicator;
    private int indicatorGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

    private float ratio = 0;
    private boolean isLoop = true;
    private long interval = INTERVAL;
    private boolean autoSwitch = true;

    private OnSlideClickListener onSlideClickListener;

    private boolean attached;

    private class Adapter extends PagerAdapter {

        @Override
        public int getCount() {
            return sliderProvider == null || sliderProvider.getCount() == 0 ? 0 : (isLoop ? Integer.MAX_VALUE : sliderProvider.getCount());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % sliderProvider.getCount();
            View view = sliderProvider.getView(getContext(), position, null);
            view.setTag(position);
            container.addView(view);
            view.setOnClickListener(Slider.this);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Slider);
            ratio = ta.getFloat(R.styleable.Slider_sliderRatio, 0f);

            isLoop = ta.getBoolean(R.styleable.Slider_sliderLoop, isLoop);

            indicatorType = ta.getInt(R.styleable.Slider_sliderIndicator, indicatorType);

            indicatorGravity = ta.getInt(R.styleable.Slider_sliderIndicatorGravity, indicatorGravity);

            autoSwitch = ta.getBoolean(R.styleable.Slider_sliderAutoSwitch, autoSwitch);

            ta.recycle();
        }

        handler = new Handler();
    }

    public void setOnSlideClickListener(OnSlideClickListener onSlideClickListener) {
        this.onSlideClickListener = onSlideClickListener;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof Integer && onSlideClickListener != null) {
            onSlideClickListener.onSlideClick(this, (Integer) v.getTag());
        }
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
        adapter.notifyDataSetChanged();
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
        requestLayout();
    }

    public void setIndicatorType(int indicatorType) {
        this.indicatorType = indicatorType;
        if (indicator != null) {
            removeView(indicator);
            indicator = null;
        }

        setupIndicator();
    }

    public Indicator getIndicator() {
        return indicator;
    }

    private void setupIndicator() {
        if (indicator == null) {
            indicator = createIndicator();
            addView(indicator);
        }

        if (sliderProvider != null && sliderProvider.getCount() != 0) {
            indicator.setCount(sliderProvider.getCount());
            indicator.setPosition(viewPager.getCurrentItem() % sliderProvider.getCount());
        } else {
            indicator.setCount(0);
        }
    }

    public void setIndicatorGravity(int gravity) {
        indicatorGravity = gravity;

        if (indicator != null) {
            LayoutParams lp = createRelativeLP(indicatorGravity);
            lp.leftMargin = lp.rightMargin = lp.topMargin = lp.bottomMargin = DensityUtil.dip2px(getContext(), 10);
            indicator.setLayoutParams(lp);
        }
    }

    public void setRollingInterval(int interval) {
        this.interval = interval;
    }

    public void setAutoSwitch(boolean autoSwitch) {
        this.autoSwitch = autoSwitch;
    }

    public int getCurrentPosition() {
        if (viewPager == null) {
            return -1;
        }
        return viewPager.getCurrentItem() % sliderProvider.getCount();
    }

    public void setCurrentPosition(int position) {
        if (viewPager != null) {
            viewPager.setCurrentItem(position);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (ratio > 0) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
                if (heightMode == MeasureSpec.EXACTLY) {
                    height = heightSize;
                } else if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(heightSize, (int) (width / ratio + .5f));
                } else {
                    height = (int) (width / ratio + .5f);
                }

                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            } else if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
                if (widthMode == MeasureSpec.EXACTLY) {
                    width = widthSize;
                } else if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(widthSize, (int) (height * ratio + .5f));
                } else {
                    width = (int) (height * ratio + .5f);
                }
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 类似设置listview的adapter
     *
     * @param provider
     */
    public void setSliderProvider(SliderProvider provider) {
        pause();

        sliderProvider = provider;

        removeAllViewsInLayout();
        viewPager = null;
        indicator = null;

        viewPager = new sNajdanViewPager(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(lp);
        addView(viewPager);
        adapter = new Adapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);


        setupIndicator();

        viewPager.setScrollable(sliderProvider != null && sliderProvider.getCount() > 1);

        if (autoSwitch && sliderProvider != null && sliderProvider.getCount() > 0) {
            resume();
        } else {
            pause();
        }
    }

    private Indicator createIndicator() {
        Indicator indicator = null;
        if (indicatorType == INDICATOR_TYPE_DOT) {
            indicator = new DotIndicator(getContext());
        } else if (indicatorType == INDICATOR_TYPE_NUMBER) {
            indicator = new NumberIndicator(getContext());
        }

        LayoutParams lp = createRelativeLP(indicatorGravity);
        lp.leftMargin = lp.rightMargin = lp.topMargin = lp.bottomMargin = DensityUtil.dip2px(getContext(), 10);
        indicator.setLayoutParams(lp);

        return indicator;
    }

    private LayoutParams createRelativeLP(int gravity) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            lp.addRule(ALIGN_PARENT_TOP);
        }
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            lp.addRule(ALIGN_PARENT_BOTTOM);
        }
        if ((gravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL) {
            lp.addRule(CENTER_VERTICAL);
        }

        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            lp.addRule(ALIGN_PARENT_LEFT);
        }
        if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            lp.addRule(ALIGN_PARENT_RIGHT);
        }
        if ((gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
            lp.addRule(CENTER_HORIZONTAL);
        }

        return lp;
    }


    /* 以下处理滚动 */

    private boolean rolling = false;

    private RollRunnable rollRunnable = new RollRunnable(this);

    private static class RollRunnable implements Runnable {

        /**
         * 防止内存泄漏
         */
        WeakReference<Slider> ref;

        public RollRunnable(Slider slider) {
            this.ref = new WeakReference<Slider>(slider);
        }

        @Override
        public void run() {
            Slider slider = ref.get();
            if (slider == null) {
                return;
            }
            if (slider.rolling) {
                slider.rollSlide();
            }
        }
    }

    public void resume() {
        if (sliderProvider == null || sliderProvider.getCount() <= 1) {
            return;
        }

        if (rolling) {
            return;
        }
        rolling = true;
        handler.postDelayed(rollRunnable, interval);
    }

    public void pause() {
        rolling = false;
        handler.removeCallbacks(rollRunnable);
    }

    private void rollSlide() {
        if (!attached) {
            return;
        }
        int current = viewPager.getCurrentItem();
        int next = (current + 1) % adapter.getCount();
        viewPager.setCurrentItem(next, next != 0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (sliderProvider != null) {
            indicator.setPosition(position % sliderProvider.getCount());

            handler.removeCallbacks(rollRunnable);
            if (rolling) {
                handler.postDelayed(rollRunnable, interval);
            }
        }
    }

    private Boolean statusBeforeTouch;

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
            if (statusBeforeTouch != null && statusBeforeTouch) {
                resume();
                statusBeforeTouch = null;
            }
        } else {
            if (statusBeforeTouch == null) {
                statusBeforeTouch = rolling;
            }
            pause();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        attached = true;
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        super.onDetachedFromWindow();
    }
}
