package com.vicpin.cleanrecyclerview.view.presenter

import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpin.cleanrecyclerview.domain.LoadNextPageCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpinm.autosubscription.Unsubscriber
import com.vicpinm.autosubscription.anotations.AutoSubscription

/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenterImpl<ViewEntity, DataEntity>
(mUseCase: GetDataCase<ViewEntity, DataEntity>, mLoadNextPageCase: LoadNextPageCase<ViewEntity, DataEntity>, observableDbMode: Boolean = false, availableDatasources: MutableList<CRDataSource>)
    : CleanListPresenter<ViewEntity, DataEntity, CleanListPresenterImpl.View<ViewEntity>>(observableDbMode, availableDatasources) {

    @AutoSubscription override val dataCase = mUseCase
    @AutoSubscription override val loadNextPageCase = mLoadNextPageCase

    override var pageLimit = 0

    fun destroyView(){
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

    }


}
