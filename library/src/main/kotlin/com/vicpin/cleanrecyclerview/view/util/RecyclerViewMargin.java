package com.vicpin.cleanrecyclerview.view.util;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Victor on 20/01/2017.
 */
public class RecyclerViewMargin extends RecyclerView.ItemDecoration{

    private final int columns;
    private int margin;


    public RecyclerViewMargin(@IntRange(from=0)int margin ) {
        this.margin = margin;
        this.columns=1;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);
        //set right margin to all
        outRect.right = margin;
        //set bottom margin to all
        outRect.bottom = margin;
        //we only add top margin to the first row
        if (position <columns) {
            outRect.top = margin;
        }
        //add left margin only to the first column
        if(position%columns==0){
            outRect.left = margin;
        }
    }

}
