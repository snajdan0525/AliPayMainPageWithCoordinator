package com.snalopainen.coordinatorlayout.alipay.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.snalopainen.coordinatorlayout.alipay.demo.model.Action;
import com.snalopainen.coordinatorlayout.alipay.demo.recycleview.ListDivider;
import com.snalopainen.coordinatorlayout.alipay.demo.recycleview.RecyclerViewBaseAdapter;
import com.snalopainen.coordinatorlayout.alipay.demo.recycleview.SimpleViewHolder;
import com.snalopainen.coordinatorlayout.alipay.demo.widget.OperationActionNavigationView;

import java.util.ArrayList;

public class AlipayMainPageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);


        CardView cardView = (CardView) findViewById(R.id.edit_search);
        cardView.setRadius(20);//设置图片圆角的半径大小
        cardView.setCardElevation(8);//设置阴影部分大小
        cardView.setContentPadding(5, 5, 5, 5);//设置图片距离阴影大小
      //  recyclerView = (RecyclerView) findViewById(R.id.rv);
        //initAdapter();
//      recyclerView.setAdapter(new RecyclerView.Adapter() {
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                return new ViewHolder(getLayoutInflater().inflate(R.layout.item_simple, parent, false));
//            }
//
//            @Override
//            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//                ViewHolder vh = (ViewHolder) holder;
//                vh.text.setText("Item " + (position + 1));
//                vh.text2.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
//            }
//
//            @Override
//            public int getItemCount() {
//                return 20;
//            }
//
//            class ViewHolder extends RecyclerView.ViewHolder {
//                TextView text;
//                TextView text2;
//
//                public ViewHolder(View itemView) {
//                    super(itemView);
//
//                    text = (TextView) itemView.findViewById(R.id.text);
//                    text2 = (TextView) itemView.findViewById(R.id.text2);
//                }
//
//            }
//        });
    }

    private ArrayList<Action> actions = new ArrayList<>(3);
    private int resIds[] = {R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3, R.drawable.icon_4, R.drawable.icon_5,
            R.drawable.icon_6, R.drawable.icon_7, R.drawable.icon_8, R.drawable.icon_9,
            R.drawable.icon_2, R.drawable.icon_5, R.drawable.icon_8};

    private void initAdapter() {
        for (int i = 0; i < 12; i++) {
            Action action = new Action();
            action.TypeId = resIds[i];
            action.TypeName = "测试字符串";
            actions.add(action);
        }
        int lineColor = 0xffe5e5e5;
        recyclerView.addItemDecoration(new ListDivider(AlipayMainPageActivity.this, LinearLayoutManager.VERTICAL, lineColor));
        recyclerView.setAdapter(new RecyclerViewBaseAdapter<ArrayList<Action>, SimpleViewHolder>() {
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
        });
    }
}
