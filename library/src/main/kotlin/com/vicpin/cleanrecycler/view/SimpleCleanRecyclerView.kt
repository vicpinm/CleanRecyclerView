package com.vicpin.cleanrecycler.view

import android.content.Context
import android.util.AttributeSet
import com.vicpin.cleanrecycler.view.CleanRecyclerView

/**
 * Created by Victor on 20/01/2017.
 */

class SimpleCleanRecyclerView<ViewEntity : Any> : CleanRecyclerView<ViewEntity, ViewEntity> {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}