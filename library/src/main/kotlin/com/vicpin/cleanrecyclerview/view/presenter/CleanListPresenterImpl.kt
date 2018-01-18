package com.vicpin.cleanrecyclerview.view.presenter

import com.vicpin.cleanrecyclerview.domain.PagedDataCase
import com.vicpinm.autosubscription.Unsubscriber
import com.vicpinm.autosubscription.anotations.AutoSubscription

/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenterImpl<T>
constructor(mUseCase: PagedDataCase<T>) : CleanListPresenter<T, CleanListPresenterImpl.View<T>>() {

    @AutoSubscription override val dataCase = mUseCase
    override var pageLimit = 0

    fun destroyView() {
        Unsubscriber.unlink(this)
    }

    interface View<T> : ICleanRecyclerView<T> {
        override fun showProgress()

        override fun hideProgress()

        override fun addData(data: List<T>)

        override fun hideLoadMore()

        override fun showLoadMore()

        override fun setData(data: List<T>)

        override fun showRefreshing()

        override fun hideRefreshing()

        override fun showEmptyLayout()

        override fun hideEmptyLayout()

        override fun notifyConnectionError()
    }


}
