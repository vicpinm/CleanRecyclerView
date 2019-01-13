package com.vicpin.cleanrecycler.sample.view.adapter

import android.view.View
import com.vicpin.cleanrecycler.sample.extensions.load
import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.cleanrecycler.sample.view.presenter.AdapterItemPresenter

import com.vicpin.kpresenteradapter.ViewHolder
import kotlinx.android.synthetic.main.adapter_item.view.*

/**
 * Created by Alvaro on 21/12/2016.
 */

class AdapterItemView(itemView: View) : ViewHolder<Item>(itemView), AdapterItemPresenter.View {

    override val presenter = AdapterItemPresenter()

    override fun setTitle(title: String) {
        itemView.title.text = title
    }

    override fun setDescription(description: String) {
        itemView.description.text = description
    }

    override fun setImage(url: String) {
        itemView.image.load(url)
    }
}
