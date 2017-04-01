package com.snalopainen.coordinatorlayout.alipay.demo.behavior;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.snalopainen.coordinatorlayout.alipay.demo.R;

import java.lang.ref.WeakReference;

/**
 * Created by snajdan on 2017/30/3.
 */

public class ToolbarAnimationBehavior extends CoordinatorLayout.Behavior {

    private WeakReference<View> dependentView;
    private ArgbEvaluator argbEvaluator;

    public ToolbarAnimationBehavior(Context context, AttributeSet attrs) {
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

        // 背景
        ((LinearLayout) child.findViewById(R.id.toolbar_container)).setAlpha(1 - progress);
    /*    ((TextView) child.findViewById(R.id.toobar_component_title)).setTextColor((int) argbEvaluator.evaluate(
                progress, resources.getColor(R.color.colorInitFloatBackground), resources.getColor(R.color.blackk)));*/
        return true;
    }

    private View getDependentView() {
        return dependentView.get();
    }

}
