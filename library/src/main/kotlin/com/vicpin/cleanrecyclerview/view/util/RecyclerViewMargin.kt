package com.vicpin.cleanrecyclerview.view.util

import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by Victor on 20/01/2017.
 */
class RecyclerViewMargin(val margin: Int, val columns : Int = 1, val orientation : Int = LinearLayoutManager.VERTICAL) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {

        val position = parent.getChildLayoutPosition(view)

        if(columns == 1) {
            if(orientation == LinearLayoutManager.VERTICAL) {
                outRect.right = margin
                outRect.left = margin
                outRect.bottom = margin
                if (position == 0) {
                    outRect.top = margin
                }
            }
            else{
                if(position == 0) {
                    outRect.right = margin
                }
                outRect.left = margin
                outRect.bottom = margin
                outRect.top = margin
            }
        }
        else {
            outRect.right = margin
            outRect.bottom = margin
            if (position < columns) {
                outRect.top = margin
            }
            if(position % columns == 0){
                //first column
                outRect.left = margin
            }
        }
    }

}
