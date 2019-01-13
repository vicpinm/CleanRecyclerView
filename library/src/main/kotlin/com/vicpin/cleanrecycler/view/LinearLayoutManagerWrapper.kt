package com.vicpin.cleanrecycler.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.AttributeSet


class LinearLayoutManagerWrapper : LinearLayoutManager {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}