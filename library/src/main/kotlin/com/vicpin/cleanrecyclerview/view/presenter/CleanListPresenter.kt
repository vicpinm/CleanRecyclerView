package com.ubox.app.main.view.presenter

import android.util.Log
import com.ubox.app.pagedrecyclerview.PagedDataCase
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.view.presenter.ICleanRecyclerView
import rx.functions.Action1
import java.util.*


/**
 * Created by Victor on 20/01/2017.
 */
abstract class CleanListPresenter<Data, View : ICleanRecyclerView<Data>> {

    var mView: View? = null
    protected var itemsLoadedSize = 0
    protected var currentPage = 0

    fun init(){
        currentPage = 0
        itemsLoadedSize = 0
        Log.d("PRESENTER","init - Disabling load more")
        mView?.hideLoadMore()
    }

    fun fetchData(fromRefresh : Boolean = false) {
        Log.d("PRESENTER","fetchData " + fromRefresh + " PAGE " + currentPage)
        if(!fromRefresh) {
            showProgress()
        }
        executeUseCase()
    }

    private fun executeUseCase() {
        dataCase.currentPage = currentPage
        dataCase.execute(Action1<Pair<CRDataSource,List<Data>>>{ result-> onDataFetched(result.first, result.second)}, Action1<Throwable>{ dataLoadError(it) })
    }

    private fun onDataFetched(source: CRDataSource, data: List<Data>) {

        Log.d("PRESENTER","onDataFetched " + source.name + " size " + data.size)

        itemsLoadedSize += data.size

        if (data.isNotEmpty()) {
            loadDataIntoView(data)
        }
        else if(currentPage == 0){
            clearDataFromView()
        }

        updateRefreshingWidget(source)
        updateLoadMoreIndicator(source, data.size)
        hideProgress()

    }

    private fun updateRefreshingWidget(source : CRDataSource){

        if (currentPage == 0) {
            if (source == CRDataSource.DISK && itemsLoadedSize > 0) {
                mView?.showRefreshing()
            } else {
                mView?.hideRefreshing()
            }
        }
    }

    private fun loadDataIntoView(data : List<Data>){
        if (currentPage == 0) {
            mView?.setData(data)
        } else {
            mView?.addData(data)
        }
    }

    private fun clearDataFromView(){
        mView?.setData(ArrayList<Data>())
    }

    private fun updateLoadMoreIndicator(source: CRDataSource, itemsLoaded: Int) {

        if (source == CRDataSource.DISK) {
            mView?.hideLoadMore()
        } else {
            if (pageLimit == 0 || itemsLoaded < pageLimit) {
                mView?.hideLoadMore()
            } else {
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

    private fun dataLoadError(ex: Throwable) {
        mView?.hideProgress()
        mView?.hideRefreshing()
        ex.printStackTrace()
    }

    fun loadNextPage() {
        Log.d("PRESENTER","loadNextPage")
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
