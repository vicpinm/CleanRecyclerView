package com.vicpin.cleanrecyclerview.view.presenter

import android.util.Log
import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpin.cleanrecyclerview.domain.LoadNextPageCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import java.util.*


/**
 * Created by Victor on 20/01/2017.
 */
abstract class CleanListPresenter<ViewEntity, DataEntity, View : ICleanRecyclerView<ViewEntity>>(val observableDbMode: Boolean, val availableDatasources: List<CRDataSource>) {

    var mView: View? = null
    protected var itemsLoadedSize = 0
    protected var currentPage = 0
    private var isShowingLoadMore = false
    private var firstPageLoadedFromCloud = false

    fun init(){
        currentPage = 0
        hideLoadMore()
    }

    fun fetchData(fromRefresh : Boolean = false, onlyDisk : Boolean = false) {
        if(!fromRefresh) {
            showProgress()
        } else if(itemsLoadedSize == 0 && mView?.isShowingPlaceholder() == false) {
            showProgress()
        }

        dataCase.onlyDisk = onlyDisk
        executeUseCase()
    }

    private fun executeUseCase() {
        if(currentPage > 0 && observableDbMode) {
            loadNextPageCase.currentPage = currentPage
            loadNextPageCase.execute({ result -> updateLoadMoreIndicator(result.first, result.second.size) }) //Results will be propagated throw dataCase, as we are in observableDbMode
        } else {
            dataCase.currentPage = currentPage
            dataCase.execute({ result -> onDataFetched(result.first, result.second) },
                    { dataLoadError(it) },
                    { dataLoadCompleted() })
        }
    }

    private fun onDataFetched(source: CRDataSource, data: List<ViewEntity>) {
        Log.e("aa","source $source size ${data.size}")
        if(currentPage == 0 && source == CRDataSource.DISK && data.isEmpty()) {
            val hadDataBeforeUpdating = itemsLoadedSize > 0
            itemsLoadedSize = 0
            if(mView?.isShowingPlaceholder() == false) {
                if(hadDataBeforeUpdating || onlyCacheMode()) {
                    mView?.showEmptyLayout()
                    mView?.hideProgress()
                } else {
                    mView?.showProgress()
                }
            }
        } else {
            itemsLoadedSize += data.size
        }


        if(!observableDbMode || source == CRDataSource.DISK) {
            if (data.isNotEmpty()) {
                loadDataIntoView(data)
            } else if (currentPage == 0) {
                clearDataFromView()
            }
        }

        updateRefreshingWidget(source)
        updateLoadMoreIndicator(source, data.size)

        if(observableDbMode && (!availableDatasources.contains(CRDataSource.CLOUD) || source == CRDataSource.CLOUD)) {
            //Data feched from cloud, or cloud is not used
            //In observableDbMode, dataLoadCompleted is not called, so we have to call it manually
            dataLoadCompleted()
        }
    }

    private fun dataLoadCompleted() {
        mView?.hideRefreshing()
        mView?.hideProgress()
        mView?.updateSwipeToRefresh(enabled = true)

        if(itemsLoadedSize == 0) {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        mView?.hideErrorLayout()
        if(mView?.hasHeaders() == false || mView?.showHeaderIfEmptyList() == false) {
            mView?.showEmptyLayout()
        }
        mView?.hideProgress()
    }


    private fun updateRefreshingWidget(source : CRDataSource){

        if (currentPage == 0) {
            if (source == CRDataSource.DISK && itemsLoadedSize > 0 && !dataCase.onlyDisk && !firstPageLoadedFromCloud) {
                mView?.showRefreshing()
            } else if(source == CRDataSource.CLOUD){
                mView?.hideRefreshing()
                firstPageLoadedFromCloud = true
            }
        }
    }

    private fun loadDataIntoView(data : List<ViewEntity>){
        mView?.hideEmptyLayout()
        mView?.hideErrorLayout()
        hideProgress()

        if (currentPage == 0 || observableDbMode) {
            mView?.setData(data)
        } else {
            mView?.addData(data)
        }
    }

    private fun clearDataFromView(){
        mView?.setData(ArrayList<ViewEntity>())
        itemsLoadedSize = 0
    }

    private fun updateLoadMoreIndicator(source: CRDataSource, itemsLoaded: Int) {

        if (source == CRDataSource.DISK && !observableDbMode) {
            hideLoadMore()
        } else {
            if (pageLimit == 0 || itemsLoaded < pageLimit) {
                hideLoadMore()
            } else {
                isShowingLoadMore = true
                mView?.showLoadMore()
            }
        }
    }



    private fun showProgress() {
        if (itemsLoadedSize == 0) {
            mView?.showProgress()
        }
    }

    private fun hideProgress() {
        if (itemsLoadedSize != 0) {
            mView?.hideProgress()
        }
    }

    private fun hideLoadMore(){
        isShowingLoadMore = false
        mView?.hideLoadMore()
    }

    private fun dataLoadError(ex: Throwable) {
        mView?.notifyConnectionError()
        mView?.hideProgress()
        mView?.hideRefreshing()
        mView?.hideEmptyLayout()
        mView?.hideErrorLayout()
        mView?.updateSwipeToRefresh(enabled = true)

        if(isShowingLoadMore){
            mView?.showLoadMoreError()
        }

        hideLoadMore()

        if(itemsLoadedSize == 0) {
            mView?.showErrorLayout()
        }

        ex.printStackTrace()
    }

    fun loadNextPage() {
        currentPage++
        fetchData()
    }

    fun refreshData() {
        currentPage = 0
        firstPageLoadedFromCloud = false
        fetchData(fromRefresh = true)
    }

    fun refreshCache() {
        currentPage = 0
        fetchData(fromRefresh = true, onlyDisk = true)
    }

    fun onlyCacheMode() = availableDatasources.size == 1 && availableDatasources.contains(CRDataSource.DISK)

    abstract val dataCase: GetDataCase<ViewEntity, DataEntity>
    abstract val loadNextPageCase: LoadNextPageCase<ViewEntity, DataEntity>

    abstract val pageLimit: Int

}
