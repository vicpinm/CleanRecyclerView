package com.vicpin.cleanrecycler.view.presenter

import android.util.Log
import com.vicpin.cleanrecycler.domain.CleanRecyclerException
import com.vicpin.cleanrecycler.domain.FROM_ERROR
import com.vicpin.cleanrecycler.domain.GetDataCase
import com.vicpin.cleanrecycler.domain.Result

/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenter<ViewEntity, DataEntity>(

        val getCachedDataCase: GetDataCase<ViewEntity, DataEntity>?,

        val getCloudDataCase: GetDataCase<ViewEntity, DataEntity>?,

        val observableDbMode: Boolean = false,

        val view: ICleanRecyclerView<ViewEntity>,

        val pageLimit: Int = 0,

        val paged: Boolean
) {

    private var currentPage = 0
    var itemsLoadedSize = 0
    private var cloudDataSourceInvoked = false

    fun destroyView() {
        getCachedDataCase?.unsubscribe()
        getCloudDataCase?.unsubscribe()
    }

    fun init() {
        currentPage = 0
        itemsLoadedSize = 0
        fetchData()
    }

    private fun fetchData() {
        getCachedDataCase?.let { fetchCachedData(it, fetchCloudDataAfter = true) }
                ?: getCloudDataCase?.let { fetchCloudData(it) }
    }

    private fun fetchCachedData(dataCase: GetDataCase<ViewEntity, DataEntity>, fetchCloudDataAfter: Boolean) {
        showProgressPlaceholderIfNeeded()

        dataCase.execute(
                onNext = {
                    onCachedDataReceived(it)
                    //After receiving disk data, fetch cloud data
                    if (fetchCloudDataAfter && !cloudDataSourceInvoked) {
                        getCloudDataCase?.let {
                            cloudDataSourceInvoked = true
                            fetchCloudData(it)
                        }
                    }
                },
                onError = {
                    view.onErrorReceived(CleanRecyclerException(it,FROM_ERROR.CACHE))
                }
        )
    }


    private fun fetchCloudData(dataCase: GetDataCase<ViewEntity, DataEntity>, nextPageLoad: Boolean = false) {
        Log.d("CleanListPresenter", "fetchCloudData --> ($currentPage -- $nextPageLoad)")

        showProgressPlaceholderIfNeeded()

        if (itemsLoadedSize > 0 && !nextPageLoad) {
            view.showRefreshing()
        }

        dataCase.with(page = currentPage).execute(
                onNext = { onCloudDataReceived(it) },
                onError = {
                    view.onErrorReceived(CleanRecyclerException(it, FROM_ERROR.CLOUD))
                    onCloudErrorReceived()
                })
    }

    private fun onCachedDataReceived(result: Result<ViewEntity>) {
        view.setData(result.data, fromCloud = false)
        view.enableRefreshing()

        itemsLoadedSize = result.size



        if (itemsLoadedSize == 0 && (getCloudDataCase == null || (cloudDataSourceInvoked && !getCloudDataCase.isInProgress))) {
            showEmptyLayout()
        } else if (itemsLoadedSize > 0) {
            view.hideProgress()
            view.hideEmptyLayout()
            view.hideErrorLayout()
        }

        if (observableDbMode) {
            updatePageIndicator()
        }
    }

    private fun onCloudDataReceived(result: Result<ViewEntity>) {
        Log.d("CleanListPresenter", "onCloudDataReceived ->> ${result.size}")
        getCloudDataCase?.unsubscribe()

        if (result.size == 0) {
            view.hideLoadMore()
        } else if (!observableDbMode) {
            //In observableDbMode, view is updated throw db notificaions,
            // so we only pass data to view here if we do not observe bbdd
            if (currentPage == 0) {
                view.setData(result.data, fromCloud = true)
                itemsLoadedSize = result.size
            } else {
                view.addData(result.data)
                itemsLoadedSize += result.size
            }

            updatePageIndicator()
        }

        view.hideProgress()
        view.hideRefreshing()
        view.hideErrorLayout()
        view.enableRefreshing()

        if (itemsLoadedSize == 0 && result.size == 0) {
            showEmptyLayout()
        }
    }

    private fun onCloudErrorReceived() {
        getCloudDataCase?.unsubscribe()

        view.hideProgress()
        view.hideRefreshing()
        view.notifyConnectionError()
        view.enableRefreshing()
        view.hideLoadMore()

        if (itemsLoadedSize == 0) {
            view.hideEmptyLayout()
            view.showErrorLayout()
        } else if (itemsLoadedSize > 0) {
            //Show toast on top of data
            view.showErrorToast()
        }

        if (currentPage > 0) {
            currentPage--
        }
    }

    private fun showEmptyLayout() {

        view.hideErrorLayout()
        view.showEmptyLayout()

        if (view.hasHeaders() && !view.showHeaderWithPlaceholder()) {
            view.hideHeaders()
        }

        view.hideProgress()
    }

    private fun showProgressPlaceholderIfNeeded() {
        if (itemsLoadedSize == 0 && !view.isShowingPlaceholder()) {
            view.showProgress()
            view.disableRefreshing()
        }
    }

    private fun updatePageIndicator() {
        if (paged && itemsLoadedSize > 0 && (pageLimit == 0 || (itemsLoadedSize >= (currentPage + 1) * pageLimit))) {
            view.showLoadMore()
        } else {
            view.hideLoadMore()
        }
    }

    fun refreshCache() {
        if (!observableDbMode && getCachedDataCase != null) {
            fetchCachedData(getCachedDataCase, fetchCloudDataAfter = false)
        }
    }

    fun refreshData() {
        view.showRefreshing()

        if (getCloudDataCase != null) {
            currentPage = 0
            view.hideLoadMore()
            fetchCloudData(getCloudDataCase)
        } else {
            refreshCache()
        }

    }

    fun loadNextPage() {
        currentPage++
        Log.d("CleanListPresenter", "load next  --> ${currentPage}")
        if (getCloudDataCase != null) {
            fetchCloudData(getCloudDataCase, nextPageLoad = true)
        }
    }


}
