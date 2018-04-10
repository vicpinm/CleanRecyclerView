package com.vicpin.cleanrecyclerview.view.presenter

import com.vicpin.cleanrecyclerview.domain.PagedDataCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import java.util.*


/**
 * Created by Victor on 20/01/2017.
 */
abstract class CleanListPresenter<ViewEntity, DataEntity, View : ICleanRecyclerView<ViewEntity>> {

    var mView: View? = null
    protected var itemsLoadedSize = 0
    protected var currentPage = 0
    private var isShowingLoadMore = false

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
        dataCase.currentPage = currentPage
        dataCase.execute({ result-> onDataFetched(result.first, result.second)},
                { dataLoadError(it) },
                { dataLoadCompleted() })
    }

    private fun onDataFetched(source: CRDataSource, data: List<ViewEntity>) {

        if(currentPage == 0 && source == CRDataSource.DISK && data.isEmpty()) {
            itemsLoadedSize = 0
            if(mView?.isShowingPlaceholder() == false) {
                mView?.showProgress()
            }
        } else {
            itemsLoadedSize += data.size
        }

        if (data.isNotEmpty()) {
            loadDataIntoView(data)
        }
        else if(currentPage == 0){
            clearDataFromView()
        }

        updateRefreshingWidget(source)
        updateLoadMoreIndicator(source, data.size)
    }

    private fun dataLoadCompleted() {
        mView?.hideRefreshing()
        mView?.hideProgress()
        mView?.setRefreshEnabled(true)

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
            if (source == CRDataSource.DISK && itemsLoadedSize > 0 && !dataCase.onlyDisk) {
                mView?.showRefreshing()
            } else if(source == CRDataSource.CLOUD){
                mView?.hideRefreshing()
            }
        }
    }

    private fun loadDataIntoView(data : List<ViewEntity>){
        mView?.hideEmptyLayout()
        mView?.hideErrorLayout()
        hideProgress()

        if (currentPage == 0) {
            mView?.setData(data)
        } else {
            mView?.addData(data)
        }
    }

    private fun clearDataFromView(){
        if(itemsLoadedSize > 0) {
            mView?.setData(ArrayList<ViewEntity>())
            itemsLoadedSize = 0
        }
    }

    private fun updateLoadMoreIndicator(source: CRDataSource, itemsLoaded: Int) {

        if (source == CRDataSource.DISK) {
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
        mView?.setRefreshEnabled(true)

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
        fetchData(fromRefresh = true)
    }

    fun refreshCache() {
        currentPage = 0
        fetchData(fromRefresh = true, onlyDisk = true)
    }

    abstract val dataCase: PagedDataCase<ViewEntity, DataEntity>

    abstract val pageLimit: Int

}
