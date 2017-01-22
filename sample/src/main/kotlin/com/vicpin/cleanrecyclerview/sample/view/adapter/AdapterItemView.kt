package com.vicpin.cleanrecyclerview.sample.view.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.vicpin.cleanrecyclerview.sample.R
import com.vicpin.cleanrecyclerview.sample.extensions.CornersTransformation
import com.vicpin.cleanrecyclerview.sample.extensions.loadWithPicasso
import com.vicpin.cleanrecyclerview.sample.model.Item
import com.vicpin.cleanrecyclerview.sample.view.presenter.AdapterItemPresenter
import com.vicpin.presenteradapter.ViewHolder

/**
 * Created by Alvaro on 21/12/2016.
 */

class AdapterItemView(itemView: View) : ViewHolder<Item>(itemView), AdapterItemPresenter.View {

    private lateinit var presenter: AdapterItemPresenter

    @BindView(R.id.title) lateinit var mTitle: TextView
    @BindView(R.id.description) lateinit var mDescription: TextView
    @BindView(R.id.image) lateinit var mImage: ImageView

    init {
        ButterKnife.bind(this, itemView)
    }


    override fun createPresenter() {
        presenter = AdapterItemPresenter()
    }

    override fun getPresenter(): AdapterItemPresenter {
        return presenter
    }

    override fun setTitle(title: String) {
        mTitle.text = title
    }

    override fun setDescription(description: String) {
        mDescription.text = description
    }

    override fun setImage(url: String) {
        mImage.loadWithPicasso(url, CornersTransformation.CornerType.TOP)
    }
}
