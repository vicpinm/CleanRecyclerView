package com.vicpin.cleanrecyclerview.view

import android.content.Context
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.pnikosis.materialishprogress.ProgressWheel
import com.vicpin.cleanrecyclerview.R
import com.vicpin.cleanrecyclerview.domain.PagedDataCase
import com.vicpin.cleanrecyclerview.repository.ListRepository
import com.vicpin.cleanrecyclerview.repository.PagedListRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudPagedDataSource
import com.vicpin.cleanrecyclerview.view.presenter.CleanListPresenterImpl
import com.vicpin.cleanrecyclerview.view.util.RecyclerViewMargin
import com.vicpin.kpresenteradapter.PresenterAdapter
import com.vicpin.kpresenteradapter.ViewHolder
import kotlin.reflect.KClass


/**
 * Created by Victor on 20/01/2017.
 */
class CleanRecyclerView<T : Any> : RelativeLayout, CleanListPresenterImpl.View<T> {

    //public fields
    var refresh: SwipeRefreshLayout? = null
    var recyclerView: RecyclerView? = null
    var itemDecoration : RecyclerView.ItemDecoration? = null

    var layoutManager: RecyclerView.LayoutManager? = null
        set(layoutManager) {
            field = layoutManager
            refresh()
        }


    //private fields
    private var progress: ProgressWheel? = null
    private var empty: FrameLayout? = null
    private var emptyError: FrameLayout? = null
    private var adapter: PresenterAdapter<T>? = null
    private lateinit var presenter: CleanListPresenterImpl<T>
    private var clickListener: ((T, ViewHolder<T>) -> Unit) ? = null
    private var inited = false
    private var isAttached = false
    private var itemsPerPage = 0
    private var cellMargin = 10
    private var emptyLayout : Int = 0
    private var errorLayout : Int = 0
    private var errorLoadMore : Int = 0
    private var attachedListener : (() -> Unit)? = null

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
            emptyLayout = a?.getResourceId(R.styleable.CleanRecyclerView_emptyLayout, 0) ?: 0
            errorLayout = a?.getResourceId(R.styleable.CleanRecyclerView_errorLayout, 0) ?: 0
            errorLoadMore = a?.getResourceId(R.styleable.CleanRecyclerView_errorLoadMore, 0) ?: 0
        } finally {
            a?.recycle()
        }
    }

    fun setOnAttachedListener(listener : () -> Unit){
        if(inited){
            listener()
        }
        else{
            this.attachedListener = listener
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        inflateView()
        isAttached = true
        init()
        loadEmptyLayout()
        loadErrorLayout()

        if(attachedListener != null){
            this.attachedListener?.invoke()
        }
    }

    private fun inflateView() {
        inflate(context, R.layout.view_cleanrecyclerview, this)
        progress = findViewById(R.id.progress) as ProgressWheel
        refresh = findViewById(R.id.refresh) as SwipeRefreshLayout
        empty = findViewById(R.id.empty) as FrameLayout
        emptyError = findViewById(R.id.emptyError) as FrameLayout
        recyclerView = findViewById(R.id.recyclerListView) as RecyclerView
    }


    fun loadPaged(adapter: PresenterAdapter<T>, cloudDataSource: CloudPagedDataSource<T>, cacheDataSource: CacheDataSource<T>) {
        inited = false
        val repository = PagedListRepository(cacheDataSource, cloudDataSource)
        val useCase = PagedDataCase(repository)
        presenter = CleanListPresenterImpl(useCase)
        presenter.mView = this
        this.adapter = adapter
        this.adapter?.itemClickListener = { item, viewHolder -> clickListener?.invoke(item, viewHolder) }
        init()
    }

    fun loadPaged(adapter: PresenterAdapter<T>, cloudDataSource: KClass<out CloudPagedDataSource<T>>, cacheDataSource: KClass<out CacheDataSource<T>>) {
        loadPaged(adapter, cloudDataSource.java, cacheDataSource.java)
    }

    fun loadPaged(adapter: PresenterAdapter<T>, cloudDataSource: Class<out CloudPagedDataSource<T>>, cacheDataSource: Class<out CacheDataSource<T>>) {
        loadPaged(adapter, cloudDataSource.newInstance(), cacheDataSource.newInstance())
    }

    fun load(adapter: PresenterAdapter<T>, cloudDataSource: CloudDataSource<T>, cacheDataSource: CacheDataSource<T>) {
        inited = false
        val repository = ListRepository(cacheDataSource, cloudDataSource)
        val useCase = PagedDataCase(repository)
        presenter = CleanListPresenterImpl(useCase)
        presenter.mView = this
        this.adapter = adapter
        this.adapter?.itemClickListener = { item, viewHolder -> clickListener?.invoke(item, viewHolder) }
        init(paged = false)
    }

    fun load(adapter: PresenterAdapter<T>, cloudDataSource: KClass<out CloudDataSource<T>>, cacheDataSource: KClass<out CacheDataSource<T>>) {
        load(adapter, cloudDataSource.java, cacheDataSource.java)
    }

    fun load(adapter: PresenterAdapter<T>, cloudDataSource: Class<out CloudDataSource<T>>, cacheDataSource: Class<out CacheDataSource<T>>) {
        load(adapter, cloudDataSource.newInstance(), cacheDataSource.newInstance())
    }

    private fun init(paged : Boolean = true) {
        if (!inited && isAttached && adapter != null) {
            setupRecyclerView()
            presenter.pageLimit = if(paged) itemsPerPage else 0
            presenter.init()
            presenter.fetchData()
            inited = true
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
        hideEmptyLayout()
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress?.visibility = View.GONE
    }

    override fun addData(data: List<T>) {
        adapter?.addData(data)
    }

    override fun setData(data: List<T>) {
        adapter?.setData(data)
    }

    override fun showLoadMore() {
        Handler().postDelayed({ adapter?.enableLoadMore { presenter.loadNextPage() }},150)
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

    fun onItemClick(listener: ((T, ViewHolder<T>) -> Unit)) {
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

    fun setEmptyLayout(@LayoutRes layoutRes : Int){
        this.emptyLayout = layoutRes
        loadEmptyLayout()
    }

    fun setErrorLayout(@LayoutRes layoutRes : Int){
        this.errorLayout = layoutRes
        loadErrorLayout()
    }

    fun setErrorLoadMore(@StringRes stringRes : Int){
        this.errorLoadMore = stringRes
    }

    fun loadEmptyLayout() {
        if (emptyLayout > 0) {
            if (empty?.childCount == 0) {
                val view = View.inflate(context, emptyLayout, null)
                empty?.addView(view)
            }
        }
    }

    fun loadErrorLayout(){
        if(errorLayout > 0){
            if(emptyError?.childCount == 0) {
                val view = View.inflate(context, errorLayout, null)
                emptyError?.addView(view)
            }
        }
    }

    override fun showEmptyLayout() {
        empty?.visibility = View.VISIBLE
    }

    override fun showErrorEmptyLayout() {
        emptyError?.visibility = View.VISIBLE
    }

    override fun hideEmptyLayout(){
        empty?.visibility = View.GONE
        emptyError?.visibility = View.GONE
    }

    override fun showLoadMoreError() {
        if(errorLoadMore > 0) {
            Toast.makeText(context, errorLoadMore, Toast.LENGTH_SHORT).show()
        }
    }

    fun addScrollListener(onScroll : () -> Unit, onStop : () -> Unit){
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    onScroll()
                } else {
                    onStop()
                }
            }
        })
    }
}