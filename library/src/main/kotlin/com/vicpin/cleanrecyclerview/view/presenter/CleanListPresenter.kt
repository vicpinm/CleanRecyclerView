package com.vicpin.cleanrecyclerview.view.presenter

import android.util.Log
import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpin.cleanrecyclerview.domain.LoadNextPageCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpinm.autosubscription.Unsubscriber
import com.vicpinm.autosubscription.anotations.AutoSubscription
import java.util.*


/**
 * Created by Victor on 20/01/2017.
 */
class CleanListPresenter<ViewEntity, DataEntity> (

        @AutoSubscription val dataCase: GetDataCase<ViewEntity, DataEntity>,

        @AutoSubscription val loadNextPageCase: LoadNextPageCase<ViewEntity, DataEntity>,

        val observableDbMode: Boolean = false,

        val availableDatasources: MutableList<CRDataSource>,

        val view: ICleanRecyclerView<ViewEntity>
)

{

    protected var itemsLoadedSize = 0
    protected var currentPage = 0
    private var isShowingLoadMore = false
    private var firstPageLoadedFromCloud = false

    var pageLimit = 0

    fun destroyView(){
        Unsubscriber.unlink(this)
    }

    fun init(){
        currentPage = 0
        hideLoadMore()
    }

    fun fetchData(fromRefresh : Boolean = false, onlyDisk : Boolean = false) {
        if(!fromRefresh) {
            showProgress()
        } else if(itemsLoadedSize == 0 && view.isShowingPlaceholder() == false) {
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
            if(view.isShowingPlaceholder() == false) {
                if(hadDataBeforeUpdating || onlyCacheMode()) {
                    view.showEmptyLayout()
                    view.hideProgress()
                } else {
                    view.showProgress()
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
        view.hideRefreshing()
        view.hideProgress()
        view.updateSwipeToRefresh(enabled = true)

        if(itemsLoadedSize == 0) {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        view.hideErrorLayout()
        if(view.hasHeaders() == false || view.showHeaderIfEmptyList() == false) {
            view.showEmptyLayout()
        }
        view.hideProgress()
    }


    private fun updateRefreshingWidget(source : CRDataSource){

        if (currentPage == 0) {
            if (source == CRDataSource.DISK && itemsLoadedSize > 0 && !dataCase.onlyDisk && !firstPageLoadedFromCloud) {
                view.showRefreshing()
            } else if(source == CRDataSource.CLOUD){
                view.hideRefreshing()
                firstPageLoadedFromCloud = true
            }
        }
    }

    private fun loadDataIntoView(data : List<ViewEntity>){
        view.hideEmptyLayout()
        view.hideErrorLayout()
        hideProgress()

        if (currentPage == 0 || observableDbMode) {
            view.setData(data)
        } else {
            view.addData(data)
        }
    }

    private fun clearDataFromView(){
        view.setData(ArrayList<ViewEntity>())
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
                view.showLoadMore()
            }
        }
    }



    private fun showProgress() {
        if (itemsLoadedSize == 0) {
            view.showProgress()
        }
    }

    private fun hideProgress() {
        if (itemsLoadedSize != 0) {
            view.hideProgress()
        }
    }

    private fun hideLoadMore(){
        isShowingLoadMore = false
        view.hideLoadMore()
    }

    private fun dataLoadError(ex: Throwable) {
        view.notifyConnectionError()
        view.hideProgress()
        view.hideRefreshing()
        view.hideEmptyLayout()
        view.hideErrorLayout()
        view.updateSwipeToRefresh(enabled = true)

        if(isShowingLoadMore){
            view.showLoadMoreError()
        }

        hideLoadMore()

        if(itemsLoadedSize == 0) {
            view.showErrorLayout()
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


}
