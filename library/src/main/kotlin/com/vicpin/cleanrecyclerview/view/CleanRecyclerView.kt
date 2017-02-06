package com.ubox.app.pagedrecyclerview

import android.content.Context
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.pnikosis.materialishprogress.ProgressWheel
import com.vicpin.cleanrecyclerview.R
import com.vicpin.cleanrecyclerview.repository.PagedListRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import com.vicpin.cleanrecyclerview.view.util.RecyclerViewMargin
import com.vicpin.presenteradapter.PresenterAdapter
import com.vicpin.presenteradapter.listeners.ItemClickListener
import kotlin.reflect.KClass


/**
 * Created by Victor on 20/01/2017.
 */
class CleanRecyclerView<T : Any> : RelativeLayout, CleanListPresenterImpl.View<T> {

    //public fields
    var recyclerView: RecyclerView? = null
    var itemDecoration : RecyclerView.ItemDecoration? = null

    var layoutManager: RecyclerView.LayoutManager? = null
        set(layoutManager) {
            field = layoutManager
            refresh()
        }


    //private fields
    private var progress: ProgressWheel? = null
    private var refresh: SwipeRefreshLayout? = null
    private var adapter: PresenterAdapter<T>? = null
    private lateinit var presenter: CleanListPresenterImpl<T>
    private var clickListener: ItemClickListener<T>? = null
    private var inited = false
    private var isAttached = false
    private var itemsPerPage = 0
    private var cellMargin = 10


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
       processAttrs(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        processAttrs(attrs)
    }

    fun processAttrs(attrs : AttributeSet?){
        val a = context?.theme?.obtainStyledAttributes(attrs, R.styleable.CleanRecyclerView, 0, 0)

        try {
            itemsPerPage = a?.getInt(R.styleable.CleanRecyclerView_itemsPerPage, 0) ?: 0
            cellMargin = a?.getDimensionPixelSize(R.styleable.CleanRecyclerView_cellMargin, 10) ?: 10
        } finally {
            a?.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        inflateView()
        isAttached = true
        init()
    }

    private fun inflateView() {
        inflate(context, R.layout.view_cleanrecyclerview, this)
        progress = findViewById(R.id.progress) as ProgressWheel
        refresh = findViewById(R.id.refresh) as SwipeRefreshLayout
        inflateRecyclerView()
    }

    fun inflateRecyclerView(){
        if (recyclerView == null){
            //Default recycler view
            recyclerView = RecyclerView(context)
        }
        refresh?.addView(recyclerView)
    }


    fun load(adapter: PresenterAdapter<T>, cloudDataSource: CloudDataSource<T>, cacheDataSource: CacheDataSource<T>) {
        inited = false
        val repository = PagedListRepository(cacheDataSource, cloudDataSource)
        val useCase = PagedDataCase(repository)
        presenter = CleanListPresenterImpl(useCase)
        presenter.mView = this
        this.adapter = adapter
        this.adapter?.setItemClickListener { item, viewHolder -> clickListener?.onItemClick(item, viewHolder) }
        init()
    }

    fun load(adapter: PresenterAdapter<T>, cloudDataSource: KClass<out CloudDataSource<T>>, cacheDataSource: KClass<out CacheDataSource<T>>) {
        load(adapter, cloudDataSource.java, cacheDataSource.java)
    }

    fun load(adapter: PresenterAdapter<T>, cloudDataSource: Class<out CloudDataSource<T>>, cacheDataSource: Class<out CacheDataSource<T>>) {
        load(adapter, cloudDataSource.newInstance(), cacheDataSource.newInstance())
    }

    private fun init() {
        if (!inited && isAttached && adapter != null) {
            inited = true
            setupRecyclerView()
            presenter.pageLimit = itemsPerPage
            presenter.init()
            presenter.fetchData()
        }
    }

    private fun refresh(){
        if(inited) {
            setupRecyclerView()
            adapter?.disableLoadMore()
            presenter.refreshData()
        }
    }


    private fun setupRecyclerView() {
        if(this.layoutManager == null){
            this.layoutManager = LinearLayoutManager(context)
        }
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = adapter
        updateDecoration()
        refresh?.setOnRefreshListener { presenter.refreshData() }
    }

    private fun updateDecoration(){
        recyclerView?.removeItemDecoration(itemDecoration)
        if(layoutManager is GridLayoutManager){
            itemDecoration = RecyclerViewMargin(cellMargin, (layoutManager as GridLayoutManager).spanCount, (layoutManager as GridLayoutManager).orientation)
        }
        else if(layoutManager is LinearLayoutManager) {
            itemDecoration = RecyclerViewMargin(cellMargin, 1, (layoutManager as LinearLayoutManager).orientation)
        }
        recyclerView?.addItemDecoration(itemDecoration)
    }

    override fun showProgress() {
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress?.visibility = View.GONE
    }

    override fun addData(data: List<T>) {
        adapter?.addData(data)
    }

    override fun setData(data: List<T>) {
        adapter?.data = data
    }

    override fun showLoadMore() {
        adapter?.enableLoadMore { presenter.loadNextPage() }
        Handler().postDelayed({ Log.d("PRESENTER","Load more enabled")},0)
    }

    override fun hideLoadMore() {
        Handler().postDelayed({ adapter?.disableLoadMore() },100)
    }

    override fun showRefreshing() {
        refresh?.isRefreshing = true
    }

    override fun hideRefreshing() {
        refresh?.isRefreshing = false
    }

    fun onItemClick(listener: ItemClickListener<T>) {
        this.clickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter.destroyView()
    }

    fun setCellMargin(marginPx: Int) {
        cellMargin = marginPx
        invalidate()
        requestLayout()
    }

    fun setItemsPerPage(numItems : Int){
        this.itemsPerPage = numItems
        presenter.pageLimit = numItems
    }


}