package com.vicpin.cleanrecyclerview.view.presenter

import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpinm.autosubscription.Unsubscriber
import com.vicpinm.autosubscription.anotations.AutoSubscription

/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenterImpl<ViewEntity, DataEntity>
constructor(mUseCase : GetDataCase<ViewEntity, DataEntity>) : CleanListPresenter<ViewEntity, DataEntity, CleanListPresenterImpl.View<ViewEntity>>() {

    @AutoSubscription override val dataCase = mUseCase
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
