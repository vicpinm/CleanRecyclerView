package com.vicpin.cleanrecycler.view.presenter

import com.vicpin.cleanrecycler.domain.CleanRecyclerException

/**
 * Created by Victor on 20/01/2017.
 */
interface ICleanRecyclerView<T> {
    fun showProgress()

    fun hideProgress()

    fun addData(data: List<T>)

    fun hideLoadMore()

    fun showLoadMore()

    fun setData(data: List<T>, fromCloud: Boolean)

    fun showRefreshing()

    fun hideRefreshing()

    fun showEmptyLayout()

    fun hideEmptyLayout()

    fun showErrorLayout()

    fun showErrorToast()

    fun hideErrorLayout()

    fun notifyConnectionError()

    fun hasHeaders() : Boolean

    fun showHeaderWithPlaceholder(): Boolean

    fun enableRefreshing()

    fun disableRefreshing()

    fun isShowingPlaceholder(): Boolean

    fun hideHeaders()

    fun onErrorReceived(error: CleanRecyclerException)
}
