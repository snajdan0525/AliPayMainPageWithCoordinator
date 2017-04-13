package com.snalopainen.coordinatorlayout.alipay.demo.recycleview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.handmark.pulltorefresh.library.extras.PullToRefreshRecyclerView;
import com.snalopainen.coordinatorlayout.alipay.demo.R;
import com.snalopainen.coordinatorlayout.alipay.demo.model.Action;
import com.snalopainen.coordinatorlayout.alipay.demo.widget.ImageSlider;
import com.snalopainen.coordinatorlayout.alipay.demo.widget.OperationActionNavigationView;

import java.util.ArrayList;

/**
 * Created by snajdan on 2017/4/1.
 */

public class MainPagePullRefreshRecyclerView extends PullToRefreshRecyclerView {
    private Context context;
    private ArrayList<Action> actions = new ArrayList<>(3);
    private int resIds[] = {R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3, R.drawable.icon_4, R.drawable.icon_5,
            R.drawable.icon_6, R.drawable.icon_7, R.drawable.icon_8, R.drawable.icon_9,
            R.drawable.icon_2, R.drawable.icon_5, R.drawable.icon_8};
    private ImageSlider imageSlider;
    public MainPagePullRefreshRecyclerView(Context context) {
        super(context);
        initView(context);
    }

    public MainPagePullRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        initAdapter();
    }

    private void initAdapter() {

        imageSlider = new ImageSlider(getContext(), null);
        imageSlider.setRatio(2);
        /* !!!why? 防止recyclerview自动滚动。ref: https://www.zhihu.com/question/48726700 */
        imageSlider.setFocusable(true);
        imageSlider.setFocusableInTouchMode(true);`
        for (int i = 0; i < 12; i++) {
            Action action = new Action();
            action.TypeId = resIds[i];
            action.TypeName = "测试字符串";
            actions.add(action);
        }
        int lineColor = 0xffe5e5e5;
        getRefreshableView().addItemDecoration(new ListDivider(context, LinearLayoutManager.VERTICAL, lineColor));
        getRefreshableView().setAdapter(new RecyclerViewBaseAdapter<ArrayList<Action>, SimpleViewHolder>() {
            @Override
            protected SimpleViewHolder onCreateDataViewHolder(ViewGroup parent, int viewType) {
                OperationActionNavigationView navigationView = new OperationActionNavigationView(parent.getContext(), null);
                navigationView.setBackgroundColor(getResources().getColor(R.color.common_bg));
                navigationView.bind(actions);
                return new SimpleViewHolder(navigationView);
            }

            @Override
            protected void onBindDataViewHolder(SimpleViewHolder holder, ArrayList<Action> action, int dataIndex) {
                ((OperationActionNavigationView) holder.itemView).bind(actions);
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        }.addHeaderView(imageSlider));
    }
}
