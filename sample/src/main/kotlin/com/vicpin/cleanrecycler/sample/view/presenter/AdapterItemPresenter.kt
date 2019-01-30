package com.vicpin.cleanrecycler.sample.view.presenter

import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.kpresenteradapter.ViewHolderPresenter

/**
 * Created by Alvaro on 21/12/2016.
 */

class AdapterItemPresenter : ViewHolderPresenter<Item, AdapterItemPresenter.View>() {

    override fun onCreate() {
        setContent()
    }

    private fun setContent() {
        view?.setTitle(data.title)
        view?.setDescription(data.description)
        view?.setImage(data.imageUrl)
    }

    interface View {
        fun setTitle(title: String)

        fun setDescription(description: String)

        fun setImage(url: String)
    }
}
