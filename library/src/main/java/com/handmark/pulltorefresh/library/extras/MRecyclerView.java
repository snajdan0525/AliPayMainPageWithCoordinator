package com.handmark.pulltorefresh.library.extras;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

public class MRecyclerView extends RecyclerView {

    private ArrayList<ItemDecoration> itemDecorations = new ArrayList<>();

    public MRecyclerView(Context context) {
        super(context);
    }

    public MRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void addItemDecoration(ItemDecoration decor, int index) {
        super.addItemDecoration(decor, index);
        itemDecorations.add(decor);
    }

    @Override
    public void removeItemDecoration(ItemDecoration decor) {
        super.removeItemDecoration(decor);
        itemDecorations.remove(decor);
    }

    public void removeAllItemDecoration() {
        ArrayList<ItemDecoration> removeList = new ArrayList<>(itemDecorations);
        final int size = removeList.size();
        for (int i = 0; i < size; i++) {
            removeItemDecoration(removeList.get(i));
        }
    }
}
