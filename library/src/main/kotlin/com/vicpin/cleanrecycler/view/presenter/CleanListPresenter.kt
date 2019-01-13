package com.vicpin.cleanrecyclerview.view.presenter

import android.util.Log
import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpinm.autosubscription.Unsubscriber
import com.vicpinm.autosubscription.anotations.AutoSubscription


/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenter<ViewEntity, DataEntity> (

        @AutoSubscription val getCachedDataCase: GetDataCase<ViewEntity, DataEntity>?,

        @AutoSubscription val getCloudDataCase: GetDataCase<ViewEntity, DataEntity>?,

        val observableDbMode: Boolean = false,

        val view: ICleanRecyclerView<ViewEntity>,

        val pageLimit: Int = 0,

        val paged: Boolean
)
{

    private var currentPage = 0
    var itemsLoadedSize = 0
    private var cloudDataSourceInvoked = false

    fun destroyView(){
        Unsubscriber.unlink(this)
    }

    fun init(){
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

        dataCase.execute(onNext =  {
            onCachedDataReceived(it)
            //After receiving disk data, fetch cloud data
            if(fetchCloudDataAfter && !cloudDataSourceInvoked) {
                getCloudDataCase?.let {
                    cloudDataSourceInvoked = true
                    fetchCloudData(it)
                }
            }
        })
    }


    private fun fetchCloudData(dataCase: GetDataCase<ViewEntity, DataEntity>) {
        showProgressPlaceholderIfNeeded()

        if(itemsLoadedSize > 0) {
            view.showRefreshing()
        }
        dataCase.with(page = currentPage).execute(
                onNext = { onCloudDataReceived(it)  },
                onError = { onCloudErrorReceived() })
    }

    private fun onCachedDataReceived(result: List<ViewEntity>) {
        view.setData(result)
        view.enableRefreshing()

        itemsLoadedSize = result.size



        if(itemsLoadedSize == 0 && (getCloudDataCase == null || (cloudDataSourceInvoked && !getCloudDataCase.isInProgress))) {
            showEmptyLayout()
        } else if(itemsLoadedSize > 0) {
            view.hideProgress()
            view.hideEmptyLayout()
            view.hideErrorLayout()
        }

        if(observableDbMode) {
            updatePageIndicator()
        }
    }

    private fun onCloudDataReceived(result: List<ViewEntity>) {
        getCloudDataCase?.unsubscribe()
        if(!observableDbMode) {
            //In observableDbMode, data is updated throw db notificaions, so ignore cloud notifications
            if(currentPage == 0) {
                view.setData(result)
                itemsLoadedSize = result.size
            } else {
                view.addData(result)
                itemsLoadedSize += result.size
            }

            updatePageIndicator()
        }

        view.hideProgress()
        view.hideRefreshing()
        view.hideErrorLayout()
        view.hideLoadMore()
        view.enableRefreshing()

        if(itemsLoadedSize == 0 && result.isEmpty()) {
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
        } else if(itemsLoadedSize > 0){
            //Show toast on top of data
            view.showErrorToast()
        }

        if (currentPage > 0) {
            currentPage--
        }


    }


    private fun showEmptyLayout() {

        view.hideErrorLayout()
        if(!view.hasHeaders() || !view.showHeaderIfEmptyList()) {
            view.showEmptyLayout()
        }
        view.hideProgress()
    }

    private fun showProgressPlaceholderIfNeeded() {
        if(itemsLoadedSize == 0 && !view.isShowingPlaceholder()) {
            view.showProgress()
            view.disableRefreshing()
        }
    }

    private fun updatePageIndicator() {
        if(paged && itemsLoadedSize > 0 && (pageLimit == 0 || (itemsLoadedSize >= (currentPage + 1) * pageLimit))) {
            view.showLoadMore()
        } else {
            view.hideLoadMore()
        }
    }

    fun refreshCache() {
        if(!observableDbMode && getCachedDataCase != null) {
            fetchCachedData(getCachedDataCase, fetchCloudDataAfter = false)
        }
    }

    fun refreshData() {
        if(getCloudDataCase != null) {
            currentPage = 0
            view.hideLoadMore()
            fetchCloudData(getCloudDataCase)
        } else {
            refreshCache()
        }

    }

    fun loadNextPage() {
        currentPage++
        if(getCloudDataCase != null) {
            fetchCloudData(getCloudDataCase)
        }
    }


}
