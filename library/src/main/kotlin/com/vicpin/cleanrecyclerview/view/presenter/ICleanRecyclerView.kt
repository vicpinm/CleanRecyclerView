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

    fun emptyDataWithHeader()

    fun showErrorLayout()

    fun showLoadMoreError()

    fun hideErrorLayout()

    fun notifyConnectionError()
}
