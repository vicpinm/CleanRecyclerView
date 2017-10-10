package com.vicpin.cleanrecyclerview.view.presenter

import com.vicpin.cleanrecyclerview.domain.PagedDataCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import rx.functions.Action0
import rx.functions.Action1
import java.util.*


/**
 * Created by Victor on 20/01/2017.
 */
abstract class CleanListPresenter<Data, View : ICleanRecyclerView<Data>> {

    var mView: View? = null
    protected var itemsLoadedSize = 0
    protected var currentPage = 0
    private var isShowingLoadMore = false;

    fun init(){
        currentPage = 0
        hideLoadMore()
    }

    fun fetchData(fromRefresh : Boolean = false) {
        if(!fromRefresh) {
            showProgress()
        }
        executeUseCase()
    }

    private fun executeUseCase() {
        dataCase.currentPage = currentPage
        dataCase.execute(Action1<Pair<CRDataSource,List<Data>>>{ result-> onDataFetched(result.first, result.second)},
                Action1<Throwable>{ dataLoadError(it) },
                Action0{ dataLoadCompleted() })
    }

    private fun onDataFetched(source: CRDataSource, data: List<Data>) {
        itemsLoadedSize += data.size

        if (data.isNotEmpty()) {
            loadDataIntoView(data)
        }
        else if(currentPage == 0){
            clearDataFromView()
            if(source == CRDataSource.CLOUD){
                showEmptyLayout()
            }
        }

        updateRefreshingWidget(source)
        updateLoadMoreIndicator(source, data.size)
    }

    private fun dataLoadCompleted() {
        mView?.hideRefreshing()
        mView?.hideProgress()

        if(itemsLoadedSize == 0) {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        mView?.showEmptyLayout()
        mView?.hideProgress()
    }

    private fun updateRefreshingWidget(source : CRDataSource){

        if (currentPage == 0) {
            if (source == CRDataSource.DISK && itemsLoadedSize > 0) {
                mView?.showRefreshing()
            } else if(source == CRDataSource.CLOUD){
                mView?.hideRefreshing()
            }
        }
    }

    private fun loadDataIntoView(data : List<Data>){
        mView?.hideEmptyLayout()
        mView?.hideErrorLayout()

        if (currentPage == 0) {
            mView?.setData(data)
        } else {
            mView?.addData(data)
        }
    }

    private fun clearDataFromView(){
        mView?.setData(ArrayList<Data>())
        itemsLoadedSize = 0
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
        mView?.hideProgress()
        mView?.hideRefreshing()
        mView?.hideEmptyLayout()
        mView?.hideErrorLayout()

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
        init()
        fetchData(fromRefresh = true)
    }

    abstract val dataCase: PagedDataCase<Data>

    abstract val pageLimit: Int

}
