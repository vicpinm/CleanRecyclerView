package com.vicpin.cleanrecyclerview.view

import android.content.Context
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
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
import com.vicpin.cleanrecyclerview.domain.GetDataCase
import com.vicpin.cleanrecyclerview.repository.ListRepository
import com.vicpin.cleanrecyclerview.repository.PagedListRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CacheDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.CloudPagedDataSource
import com.vicpin.cleanrecyclerview.repository.datasource.EmptyCache
import com.vicpin.cleanrecyclerview.view.interfaces.Mapper
import com.vicpin.cleanrecyclerview.view.presenter.CleanListPresenterImpl
import com.vicpin.cleanrecyclerview.view.util.DividerDecoration
import com.vicpin.cleanrecyclerview.view.util.RecyclerViewMargin
import com.vicpin.kpresenteradapter.PresenterAdapter
import com.vicpin.kpresenteradapter.SingleLinePresenterAdapter
import com.vicpin.kpresenteradapter.ViewHolder
import kotlin.reflect.KClass

/**
 * Created by Victor on 20/01/2017.
 */

open class CleanRecyclerView<ViewEntity : Any, DataEntity : Any> : RelativeLayout, CleanListPresenterImpl.View<ViewEntity> {

    //public fields
    var refresh: SwipeRefreshLayout? = null
    var recyclerView: RecyclerView? = null
    var itemDecoration: RecyclerView.ItemDecoration? = null

    var layoutManager: RecyclerView.LayoutManager? = null
        set(layoutManager) {
            field = layoutManager
            refresh()
        }

    enum class Event {
        VIEW_LOADED,
        DATA_LOADED,
        EMPTY_LAYOUT_SHOWED,
        EMPTY_LAYOUT_HIDED,
        ERROR_LAYOUT_SHOWED,
        ERROR_LAYOUT_HIDED,
        ON_REFRESH,
        CONNECTION_ERROR
    }

    //private fields
    private var progress: ProgressWheel? = null
    private var empty: FrameLayout? = null
    private var emptyError: FrameLayout? = null
    private var adapter: PresenterAdapter<ViewEntity>? = null
    private var presenter: CleanListPresenterImpl<ViewEntity, DataEntity>? = null
    private var clickListener: ((ViewEntity, ViewHolder<ViewEntity>) -> Unit)? = null
    private var inited = false
    private var isAttached = false
    private var itemsPerPage = 0
    private var cellMargin = 10
    private var emptyLayout: Int = 0
    private var errorLayout: Int = 0
    private var errorLoadMore: Int = 0
    private var eventListener: ((Event) -> Unit)? = null
    private var wrapInCardView = false
    private var dividerDrawable: Int = 0
    private var refreshEnabled = false
    private var showHeaderIfEmptyList = false

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        processAttrs(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        processAttrs(attrs)
    }

    fun processAttrs(attrs: AttributeSet?) {
        val a = context?.theme?.obtainStyledAttributes(attrs, R.styleable.CleanRecyclerView, 0, 0)

        try {
            itemsPerPage = a?.getInt(R.styleable.CleanRecyclerView_itemsPerPage, 0) ?: 0
            cellMargin = a?.getDimensionPixelSize(R.styleable.CleanRecyclerView_cellMargin, 10) ?: 10
            emptyLayout = a?.getResourceId(R.styleable.CleanRecyclerView_emptyLayout, 0) ?: 0
            errorLayout = a?.getResourceId(R.styleable.CleanRecyclerView_errorLayout, 0) ?: 0
            errorLoadMore = a?.getResourceId(R.styleable.CleanRecyclerView_errorLoadMore, 0) ?: 0
            wrapInCardView = a?.getBoolean(R.styleable.CleanRecyclerView_wrapInCardView, false) ?: false
            dividerDrawable = a?.getResourceId(R.styleable.CleanRecyclerView_dividerDrawable, 0) ?: 0
            refreshEnabled = a?.getBoolean(R.styleable.CleanRecyclerView_refreshEnabled, true) ?: true
            showHeaderIfEmptyList = a?.getBoolean(R.styleable.CleanRecyclerView_showHeaderIfEmptyList, false) ?: false
        } finally {
            a?.recycle()
        }
    }

    fun setEventListener(listener: (Event) -> Unit) {
        this.eventListener = listener
        if (inited) {
            listener(Event.VIEW_LOADED)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        inflateView()
        isAttached = true
        init()
        loadEmptyLayout()
        loadErrorLayout()

        this.eventListener?.invoke(Event.VIEW_LOADED)
    }

    private fun inflateView() {
        inflate(context, if (wrapInCardView) R.layout.view_cleanrecyclerview_cardview else R.layout.view_cleanrecyclerview, this)
        progress = findViewById(R.id.progress)
        refresh = findViewById(R.id.refresh)
        empty = findViewById(R.id.empty)
        emptyError = findViewById(R.id.emptyError)
        recyclerView = findViewById(R.id.recyclerListView)
    }

    /**
     * Load paged methods
     */
    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: CloudPagedDataSource<DataEntity, CustomData>, cache: CacheDataSource<DataEntity> = EmptyCache(), mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        inited = false
        val repository = PagedListRepository(cache, cloud, customData)
        val useCase = GetDataCase(repository, mapper)
        presenter = CleanListPresenterImpl(useCase)
        presenter?.mView = this
        this.adapter = adapter
        this.adapter?.itemClickListener = { item, viewHolder -> clickListener?.invoke(item, viewHolder) }
        init()
    }

    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: KClass<out CloudPagedDataSource<DataEntity, CustomData>>, cache: KClass<out CacheDataSource<DataEntity>>? = null, mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        loadPaged(adapter, cloud.java, cache?.java, mapper, customData)
    }

    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: Class<out CloudPagedDataSource<DataEntity, CustomData>>, cache: Class<out CacheDataSource<DataEntity>>? = null, mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        loadPaged(adapter, cloud.newInstance(), cache?.newInstance() ?: EmptyCache(), mapper, customData)
    }

    /**
     * Load methods with no pagination
     */
    fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: CloudDataSource<DataEntity, CustomData>? = null, cache: CacheDataSource<DataEntity> = EmptyCache(), mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        inited = false
        val repository = ListRepository(cache, cloud, customData)
        val useCase = GetDataCase(repository, mapper)
        presenter = CleanListPresenterImpl(useCase)
        presenter?.mView = this
        this.adapter = adapter
        this.adapter?.itemClickListener = { item, viewHolder -> clickListener?.invoke(item, viewHolder) }
        init(paged = false)
    }

    fun <CustomData> loadSingleLine(@LayoutRes layoutResId: Int, cloud: CloudDataSource<DataEntity, CustomData>? = null, cache: CacheDataSource<DataEntity> = EmptyCache(), mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(SingleLinePresenterAdapter(layoutResId), cloud, cache, mapper, customData)
    }

    fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: KClass<out CloudDataSource<DataEntity, CustomData>>? = null, cache: KClass<out CacheDataSource<DataEntity>>? = null, mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(adapter, cloud?.java, cache?.java, mapper, customData)
    }

    fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: Class<out CloudDataSource<DataEntity, CustomData>>? = null, cache: Class<out CacheDataSource<DataEntity>>? = null, mapper : Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(adapter, cloud?.newInstance(), cache?.newInstance() ?: EmptyCache(), mapper, customData)
    }

    private fun init(paged: Boolean = true) {
        if (!inited && isAttached && adapter != null) {
            setupRecyclerView()
            presenter?.pageLimit = if (paged) itemsPerPage else 0
            presenter?.init()
            presenter?.fetchData()
            inited = true
        }
    }

    private fun refresh() {
        if (inited) {
            setupRecyclerView()
            adapter?.disableLoadMore()
            presenter?.refreshData()
        }
    }

    private fun setupRecyclerView() {
        if (this.layoutManager == null) {
            this.layoutManager = LinearLayoutManager(context)
        }
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = adapter
        updateDecoration()
        refresh?.setOnRefreshListener {
            presenter?.refreshData()
            eventListener?.invoke(Event.ON_REFRESH)
        }
        setRefreshEnabled(refreshEnabled)
    }

    private fun updateDecoration() {
        recyclerView?.removeItemDecoration(itemDecoration)
        if (layoutManager is GridLayoutManager) {
            itemDecoration = RecyclerViewMargin(cellMargin, (layoutManager as GridLayoutManager).spanCount, (layoutManager as GridLayoutManager).orientation)
        } else if (layoutManager is LinearLayoutManager) {
            itemDecoration = RecyclerViewMargin(cellMargin, 1, (layoutManager as LinearLayoutManager).orientation)
        }
        recyclerView?.addItemDecoration(itemDecoration)

        if (dividerDrawable > 0 && layoutManager is LinearLayoutManager) {
            val divider = DividerDecoration(context, (layoutManager as LinearLayoutManager).orientation)
            divider.setDrawable(ContextCompat.getDrawable(context, dividerDrawable))
            recyclerView?.addItemDecoration(divider)
        }
    }

    fun reloadData() {
        presenter?.refreshData()
    }

    fun reloadCache() {
        presenter?.refreshCache()
    }

    override fun showProgress() {
        hideEmptyLayout()
        hideErrorLayout()
        adapter?.clearData()
        setRefreshEnabled(false)
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress?.visibility = View.GONE
    }

    override fun addData(data: List<ViewEntity>) {
        adapter?.addData(data)
    }

    override fun setData(data: List<ViewEntity>) {
        adapter?.setData(data)
        if (data.isNotEmpty()) {
            eventListener?.invoke(Event.DATA_LOADED)
        }
    }

    override fun showLoadMore() {
        Handler().postDelayed({ adapter?.enableLoadMore { presenter?.loadNextPage() } }, 150)
    }

    override fun hideLoadMore() {
        if(adapter?.loadMoreListener != null) {
            adapter?.loadMoreListener = null
            Handler().postDelayed({ adapter?.disableLoadMore() }, 100)
        }
    }

    override fun showRefreshing() {
        if (refreshEnabled) {
            refresh?.isRefreshing = true
        }
    }

    override fun hideRefreshing() {
        refresh?.isRefreshing = false
    }

    override fun setRefreshEnabled(enabled: Boolean) {
        refreshEnabled = enabled
        refresh?.isEnabled = enabled
    }

    fun onItemClick(listener: ((ViewEntity, ViewHolder<ViewEntity>) -> Unit)) {
        this.clickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.destroyView()
    }

    fun setCellMargin(marginPx: Int) {
        cellMargin = marginPx
        invalidate()
        requestLayout()
    }

    fun setItemsPerPage(numItems: Int) {
        this.itemsPerPage = numItems
        presenter?.pageLimit = numItems
    }

    fun setEmptyLayout(@LayoutRes layoutRes: Int) {
        this.emptyLayout = layoutRes
        loadEmptyLayout()
    }

    fun setErrorLayout(@LayoutRes layoutRes: Int) {
        this.errorLayout = layoutRes
        loadErrorLayout()
    }

    fun setErrorLoadMore(@StringRes stringRes: Int) {
        this.errorLoadMore = stringRes
    }

    fun setShowHeaderIfEmptyList(showHeader: Boolean) {
        this.showHeaderIfEmptyList = showHeader
    }

    fun loadEmptyLayout() {
        if (emptyLayout > 0) {
            if(empty?.childCount ?: 0 > 0) {
                empty?.removeAllViews()
            }
            if (empty?.childCount == 0) {
                val view = View.inflate(context, emptyLayout, null)
                empty?.addView(view)
            }

        }
    }

    fun loadErrorLayout() {
        if (errorLayout > 0) {
            if (emptyError?.childCount == 0) {
                val view = View.inflate(context, errorLayout, null)
                emptyError?.addView(view)
            }
        }
    }

    override fun showEmptyLayout() {
        emptyError?.visibility = View.GONE
        empty?.visibility = View.VISIBLE
        eventListener?.invoke(Event.EMPTY_LAYOUT_SHOWED)
    }

    override fun showErrorLayout() {
        empty?.visibility = View.GONE
        emptyError?.visibility = View.VISIBLE
        eventListener?.invoke(Event.ERROR_LAYOUT_SHOWED)
    }

    override fun hideEmptyLayout() {
        empty?.visibility = View.GONE
        eventListener?.invoke(Event.EMPTY_LAYOUT_HIDED)
    }

    override fun hideErrorLayout() {
        emptyError?.visibility = View.GONE
        eventListener?.invoke(Event.ERROR_LAYOUT_HIDED)
    }

    override fun notifyConnectionError() {
        eventListener?.invoke(Event.CONNECTION_ERROR)
    }

    override fun showLoadMoreError() {
        if (errorLoadMore > 0) {
            Toast.makeText(context, errorLoadMore, Toast.LENGTH_SHORT).show()
        }
    }

    private var overallYScroll = 0
    var isScrollingDown = false
    var isScrollingUp = false

    fun addScrollListener(onScroll: ((Int) -> Unit)? = null, onStop: ((Int) -> Unit)? = null) {
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    //Do nothing
                } else {
                    onStop?.invoke(overallYScroll)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                overallYScroll += dy
                if(dy > 0) {
                    isScrollingDown = true
                    isScrollingUp = false
                } else {
                    isScrollingDown = false
                    isScrollingUp = true
                }
                onScroll?.invoke(overallYScroll)
            }
        })
    }

    override fun hasHeaders() = if(adapter != null) adapter!!.getHeadersCount() > 0 else false

    override fun showHeaderIfEmptyList() = this.showHeaderIfEmptyList

    fun isEmpty(): Boolean {
        return adapter?.getData()?.isEmpty() ?: true
    }

    override fun isShowingPlaceholder() = empty?.visibility == View.VISIBLE || emptyError?.visibility == View.VISIBLE
}