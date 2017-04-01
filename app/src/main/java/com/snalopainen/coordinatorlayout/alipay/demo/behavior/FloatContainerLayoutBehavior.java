package com.snalopainen.coordinatorlayout.alipay.demo.behavior;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.snalopainen.coordinatorlayout.alipay.demo.R;

import java.lang.ref.WeakReference;

/**
 * Created by snajdan on 2017/30/3.
 */
public class FloatContainerLayoutBehavior extends CoordinatorLayout.Behavior<View> {

    private WeakReference<View> dependentView;
    private ArgbEvaluator argbEvaluator;

    public FloatContainerLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        argbEvaluator = new ArgbEvaluator();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.scrolling_header) {
            dependentView = new WeakReference<>(dependency);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        Resources resources = getDependentView().getResources();
        final float progress = 1.f -
                Math.abs(dependency.getTranslationY() / (dependency.getHeight() - resources.getDimension(R.dimen.collapsed_header_height)));
        //Scale
        float scale = 1 - 0.4f * (1.f - progress);
        child.setScaleX(scale);
        child.setScaleY(scale);

        // Translation
        final float initOffset = dependency.getHeight() - child.getHeight() / 2;
        final float translateY = initOffset + dependency.getTranslationY();
        child.setTranslationY(translateY);
        /*
        ((TextView) child.findViewById(R.id.textview)).setTextColor((int) argbEvaluator.evaluate(
                progress,
               resources.getColor(R.color.colorInitFloatBackground),
                resources.getColor(R.color.blackk)));
                */
        //Alpha
        child.setAlpha(progress);

        // Margins
        final float collapsedMargin = resources.getDimension(R.dimen.collapsed_float_margin);
        final float initMargin = resources.getDimension(R.dimen.init_float_margin);
        final int margin = (int) (collapsedMargin + (initMargin - collapsedMargin) * 1);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.setMargins(margin, 0, margin, 0);
        child.setLayoutParams(lp);
        return true;
    }

    private View getDependentView() {
        return dependentView.get();
    }
}
