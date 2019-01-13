package com.vicpin.cleanrecyclerview.view.presenter

/**
 * Created by Victor on 20/01/2017.
 */
interface ICleanRecyclerView<T> {
    fun showProgress()

    fun hideProgress()

    fun addData(data: List<T>)

    fun hideLoadMore()

    fun showLoadMore()

    fun setData(data: List<T>)

    fun showRefreshing()

    fun hideRefreshing()

    fun showEmptyLayout()

    fun hideEmptyLayout()

    fun showErrorLayout()

    fun showErrorToast()

    fun hideErrorLayout()

    fun notifyConnectionError()

    fun hasHeaders() : Boolean

    fun showHeaderIfEmptyList(): Boolean

    fun enableRefreshing()

    fun disableRefreshing()

    fun isShowingPlaceholder(): Boolean
}
