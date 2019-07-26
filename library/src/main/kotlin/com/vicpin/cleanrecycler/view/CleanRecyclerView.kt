package com.vicpin.cleanrecycler.view

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vicpin.cleanrecycler.R
import com.vicpin.cleanrecycler.domain.GetDataCase
import com.vicpin.cleanrecycler.repository.ListRepository
import com.vicpin.cleanrecycler.repository.datasource.*
import com.vicpin.cleanrecycler.view.interfaces.Mapper
import com.vicpin.cleanrecycler.view.presenter.CleanListPresenter
import com.vicpin.cleanrecycler.view.presenter.ICleanRecyclerView
import com.vicpin.cleanrecycler.view.util.DividerDecoration
import com.vicpin.cleanrecycler.view.util.RecyclerViewMargin
import com.vicpin.kpresenteradapter.PresenterAdapter
import com.vicpin.kpresenteradapter.SingleLinePresenterAdapter
import com.vicpin.kpresenteradapter.ViewHolder
import kotlin.reflect.KClass

/**
 * Created by Victor on 20/01/2017.
 */

open class CleanRecyclerView<ViewEntity : Any, DataEntity : Any> : RelativeLayout, ICleanRecyclerView<ViewEntity> {

    //public fields
    var refresh: SwipeRefreshLayout? = null
    var recyclerView: RecyclerView? = null
    var marginDecoration: RecyclerView.ItemDecoration? = null
    var dividerDecoration: DividerDecoration? = null
    var dataSetChangedListener: ((List<ViewEntity>) -> Unit)? = null

    var layoutManager: RecyclerView.LayoutManager? = null
        set(layoutManager) {
            val refreshAfterChange = field != null
            field = layoutManager
            if (refreshAfterChange) refresh()
        }

    enum class Event {
        VIEW_LOADED,
        DATA_LOADED, //Data loaded from any source event
        DATA_LOADED_FROM_CLOUD,
        DATA_LOADED_FROM_CACHE,
        EMPTY_LAYOUT_SHOWED,
        EMPTY_LAYOUT_HIDED,
        ERROR_LAYOUT_SHOWED,
        ERROR_LAYOUT_HIDED,
        ON_REFRESH,
        CONNECTION_ERROR
    }

    //private fields
    private var progress: ProgressBar? = null
    var empty: FrameLayout? = null
    var emptyError: FrameLayout? = null
    private var adapter: PresenterAdapter<ViewEntity>? = null
    private var presenter: CleanListPresenter<ViewEntity, DataEntity>? = null
    private var clickListener: ((ViewEntity, ViewHolder<ViewEntity>) -> Unit)? = null
    private var inited = false
    private var isAttached = false
    private var itemsPerPage = 0
    private var cellMargin = 0
    private var emptyLayout: Int = 0
    private var errorLayout: Int = 0
    private var errorToast: Int = 0
    private var eventListener: ((Event) -> Unit)? = null
    private var wrapInCardView = false
    private var dividerDrawable: Int = 0
    private var refreshEnabled = false
    private var showHeaderWithPlaceholder = false
    private var wrapInNestedScroll = false
    private var progressBarColor = 0

    constructor(context: Context?) : super(context) {
        inflate()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        processAttrs(attrs)
        inflate()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        processAttrs(attrs)
        inflate()
    }

    fun processAttrs(attrs: AttributeSet?) {
        val a = context?.theme?.obtainStyledAttributes(attrs, R.styleable.CleanRecyclerView, 0, 0)

        try {
            itemsPerPage = a?.getInt(R.styleable.CleanRecyclerView_itemsPerPage, 0) ?: 0
            cellMargin = a?.getDimensionPixelSize(R.styleable.CleanRecyclerView_cellMargin, 0) ?: 0
            emptyLayout = a?.getResourceId(R.styleable.CleanRecyclerView_emptyLayout, 0) ?: 0
            errorLayout = a?.getResourceId(R.styleable.CleanRecyclerView_errorLayout, 0) ?: 0
            errorToast = a?.getResourceId(R.styleable.CleanRecyclerView_errorToast, 0) ?: 0
            wrapInCardView = a?.getBoolean(R.styleable.CleanRecyclerView_wrapInCardView, false)
                    ?: false
            dividerDrawable = a?.getResourceId(R.styleable.CleanRecyclerView_dividerDrawable, 0)
                    ?: 0
            refreshEnabled = a?.getBoolean(R.styleable.CleanRecyclerView_refreshEnabled, true)
                    ?: true
            showHeaderWithPlaceholder = a?.getBoolean(R.styleable.CleanRecyclerView_showHeaderWithPlaceholder, false)
                    ?: false
            wrapInNestedScroll = a?.getBoolean(R.styleable.CleanRecyclerView_wrapInNestedScroll, false)
                    ?: false
            progressBarColor = a?.getResourceId(R.styleable.CleanRecyclerView_progressBarColor, R.color.default_progress_bar_color)
                    ?: R.color.default_progress_bar_color
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

    private fun inflate() {
        inflateView()
        isAttached = true
        loadEmptyLayout()
        loadErrorLayout()

        this.eventListener?.invoke(Event.VIEW_LOADED)
    }

    private fun inflateView() {
        val layout = if (wrapInCardView) {
            R.layout.view_cleanrecyclerview_cardview
        } else if (wrapInNestedScroll) {
            R.layout.view_cleanrecyclerview_nestedscroll
        } else {
            R.layout.view_cleanrecyclerview
        }

        inflate(context, layout, this)
        progress = findViewById(R.id.progress)


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            progress?.let {
                val wrapDrawable = DrawableCompat.wrap(it.indeterminateDrawable)
                DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context, progressBarColor))
                it.indeterminateDrawable = DrawableCompat.unwrap(wrapDrawable)
            }
        } else {
            progress?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(context, progressBarColor), PorterDuff.Mode.SRC_IN)
        }

        refresh = findViewById(R.id.refresh)
        empty = findViewById(R.id.empty)
        emptyError = findViewById(R.id.emptyError)
        recyclerView = findViewById(R.id.recyclerListView)
        this.adapter?.notifyScrollStatus(recyclerView!!)

        if (wrapInNestedScroll) {
            recyclerView?.isNestedScrollingEnabled = false
        }
    }

    /**
     * Load paged methods
     */
    @JvmOverloads
    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: CloudParamPagedDataSource<DataEntity, CustomData>? = null, cache: ParamCacheDataSource<DataEntity, CustomData>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        inited = false

        val observableCache = cache != null && cache !is SingleParamCacheDataSource

        val repository = ListRepository(cache, cloud, customData)
        val cloudDataCase = if (cloud != null) GetDataCase(repository, mapper, CRDataSource.CLOUD, observableCache) else null
        val cachedDataCase = if (cache != null) GetDataCase(repository, mapper, CRDataSource.DISK, observableCache) else null


        presenter = CleanListPresenter(cachedDataCase, cloudDataCase, observableCache, this, itemsPerPage, paged = cloudDataCase != null)
        this.adapter = adapter
        clickListener?.let {
            this.adapter?.itemClickListener = { item, viewHolder -> it.invoke(item, viewHolder) }
        }
        init()
    }

    @JvmOverloads
    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: KClass<out CloudParamPagedDataSource<DataEntity, CustomData>>, cache: KClass<out ParamCacheDataSource<DataEntity, CustomData>>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        loadPaged(adapter, cloud.java, cache?.java, mapper, customData)
    }

    fun <CustomData> loadPaged(adapter: PresenterAdapter<ViewEntity>, cloud: Class<out CloudParamPagedDataSource<DataEntity, CustomData>>? = null, cache: Class<out ParamCacheDataSource<DataEntity, CustomData>>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        loadPaged(adapter, cloud?.newInstance(), cache?.newInstance(), mapper, customData)
    }

    /**
     * Load methods with no pagination
     */
    @JvmOverloads
    open fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: CloudParamDataSource<DataEntity, CustomData>? = null, cache: ParamCacheDataSource<DataEntity, CustomData>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        inited = false

        val observableCache = cache != null && cache !is SingleParamCacheDataSource

        val repository = ListRepository(cache, cloud, customData)
        val cloudDataCase = if (cloud != null) GetDataCase(repository, mapper, CRDataSource.CLOUD, observableCache) else null
        val cachedDataCase = if (cache != null) GetDataCase(repository, mapper, CRDataSource.DISK, observableCache) else null


        presenter = CleanListPresenter(cachedDataCase, cloudDataCase, observableCache, this, itemsPerPage, paged = false)
        this.adapter = adapter
        clickListener?.let {
            this.adapter?.itemClickListener = { item, viewHolder -> it.invoke(item, viewHolder) }
        }
        init()
    }

    @JvmOverloads
    fun <CustomData> loadSingleLine(@LayoutRes layoutResId: Int, cloud: CloudParamDataSource<DataEntity, CustomData>? = null, cache: ParamCacheDataSource<DataEntity, CustomData>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(SingleLinePresenterAdapter(layoutResId), cloud, cache, mapper, customData)
    }

    open fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: KClass<out CloudParamDataSource<DataEntity, CustomData>>? = null, cache: KClass<out ParamCacheDataSource<DataEntity, CustomData>>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(adapter, cloud?.java, cache?.java, mapper, customData)
    }

    open fun <CustomData> load(adapter: PresenterAdapter<ViewEntity>, cloud: Class<out CloudParamDataSource<DataEntity, CustomData>>? = null, cache: Class<out ParamCacheDataSource<DataEntity, CustomData>>? = null, mapper: Mapper<ViewEntity, DataEntity>? = null, customData: CustomData? = null) {
        load(adapter, cloud?.newInstance(), cache?.newInstance(), mapper, customData)
    }

    private fun init() {
        if (!inited && isAttached && adapter != null) {
            setupRecyclerView()
            presenter?.init()
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
            this.layoutManager = LinearLayoutManagerWrapper(context)
        }
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = adapter
        updateDecoration()
        refresh?.setOnRefreshListener {
            presenter?.refreshData()
            eventListener?.invoke(Event.ON_REFRESH)
        }

        recyclerView?.addOnScrollListener(scrollListener())
        setRefreshEnabled(refreshEnabled)
    }

    private fun updateDecoration() {
        marginDecoration?.let { recyclerView?.removeItemDecoration(it) }

        if (cellMargin > 0) {
            if (layoutManager is GridLayoutManager) {
                marginDecoration = RecyclerViewMargin(cellMargin, (layoutManager as GridLayoutManager).spanCount, (layoutManager as GridLayoutManager).orientation)
            } else if (layoutManager is LinearLayoutManager) {
                marginDecoration = RecyclerViewMargin(cellMargin, 1, (layoutManager as LinearLayoutManager).orientation)
            }
            marginDecoration?.let { recyclerView?.addItemDecoration(it) }
        }

        addDividerDecoration()
    }

    private fun addDividerDecoration() {
        dividerDecoration?.let { recyclerView?.removeItemDecoration(it) }

        if (dividerDrawable > 0 && layoutManager is LinearLayoutManager) {
            dividerDecoration = DividerDecoration(context, (layoutManager as LinearLayoutManager).orientation)
            ContextCompat.getDrawable(context, dividerDrawable)?.let { dividerDecoration?.setDrawable(it) }
            recyclerView?.addItemDecoration(dividerDecoration!!)
        }
    }

    fun setDividerDrawable(@DrawableRes resId: Int) {
        this.dividerDrawable = resId
        addDividerDecoration()
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
        disableRefreshing()
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress?.visibility = View.GONE
    }

    override fun addData(data: List<ViewEntity>) {
        recyclerView?.visibility = View.VISIBLE
        dataSetChangedListener?.invoke(adapter?.getData() ?: listOf<ViewEntity>() + data)
        adapter?.addData(data)
    }

    override fun setData(data: List<ViewEntity>, fromCloud: Boolean) {
        recyclerView?.visibility = View.VISIBLE
        dataSetChangedListener?.invoke(data)

        adapter?.setData(data)
        if (data.isNotEmpty()) {
            eventListener?.invoke(Event.DATA_LOADED)
            eventListener?.invoke(if (fromCloud) Event.DATA_LOADED_FROM_CLOUD else Event.DATA_LOADED_FROM_CACHE)
        }
    }

    override fun showLoadMore() {
        Handler().postDelayed({ adapter?.enableLoadMore { presenter?.loadNextPage() } }, 200)
    }

    override fun hideLoadMore() {
        if (adapter?.loadMoreListener != null) {
            adapter?.loadMoreListener = null
            Handler().postDelayed({ adapter?.disableLoadMore() }, 200)
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

    override fun enableRefreshing() {
        if (refreshEnabled && isListAtTop()) {
            refresh?.isEnabled = true
        }
    }

    override fun disableRefreshing() {
        refresh?.isEnabled = false
    }

    fun setRefreshEnabled(enabled: Boolean) {
        refreshEnabled = enabled
        if (refreshEnabled) {
            enableRefreshing()
        } else {
            disableRefreshing()
        }
    }

    fun onItemClick(listener: ((ViewEntity, ViewHolder<ViewEntity>) -> Unit)) {
        this.clickListener = listener
        adapter?.let {
            it.itemClickListener = { item, view -> listener(item, view) }
        }
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
    }

    fun setEmptyLayout(@LayoutRes layoutRes: Int) {
        this.emptyLayout = layoutRes
        loadEmptyLayout()
    }

    fun setErrorLayout(@LayoutRes layoutRes: Int) {
        this.errorLayout = layoutRes
        loadErrorLayout()
    }

    fun setErrorToast(@StringRes stringRes: Int) {
        this.errorToast = stringRes
    }

    fun setShowHeaderWithPlaceholder(showHeader: Boolean) {
        this.showHeaderWithPlaceholder = showHeader
    }

    private fun loadEmptyLayout() {
        if (emptyLayout > 0) {
            if (empty?.childCount ?: 0 > 0) {
                empty?.removeAllViews()
            }
            if (empty?.childCount == 0) {
                val view = View.inflate(context, emptyLayout, null)
                empty?.addView(view)
            }
        }
    }

    private fun loadErrorLayout() {
        if (errorLayout > 0) {
            if (emptyError?.childCount ?: 0 > 0) {
                emptyError?.removeAllViews()
            }
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

        //if(showHeaderWithPlaceholder) {
        recyclerView?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //}
    }

    override fun showErrorLayout() {
        empty?.visibility = View.GONE
        emptyError?.visibility = View.VISIBLE
        eventListener?.invoke(Event.ERROR_LAYOUT_SHOWED)

        if (showHeaderWithPlaceholder) {
            recyclerView?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun hideEmptyLayout() {
        empty?.visibility = View.GONE
        eventListener?.invoke(Event.EMPTY_LAYOUT_HIDED)

        if (showHeaderWithPlaceholder) {
            recyclerView?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun hideErrorLayout() {
        emptyError?.visibility = View.GONE
        eventListener?.invoke(Event.ERROR_LAYOUT_HIDED)

        if (showHeaderWithPlaceholder) {
            recyclerView?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun notifyConnectionError() {
        eventListener?.invoke(Event.CONNECTION_ERROR)
    }

    override fun showErrorToast() {
        if (errorToast > 0) {
            Toast.makeText(context, errorToast, Toast.LENGTH_SHORT).show()
        }
    }

    private var overallYScroll = 0
    var isScrollingDown = false
    var isScrollingUp = false

    private fun scrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            overallYScroll += dy
            if (isListAtTop()) {
                enableRefreshing()
            } else {
                disableRefreshing()
            }
        }
    }

    fun addScrollListener(onScroll: ((Int) -> Unit)? = null, onStop: ((Int) -> Unit)? = null) {
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    //Do nothing
                } else {
                    onStop?.invoke(overallYScroll)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
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

    override fun hasHeaders() = if (adapter != null) adapter!!.getHeadersCount() > 0 else false

    override fun showHeaderWithPlaceholder() = this.showHeaderWithPlaceholder

    fun isEmpty(): Boolean {
        return adapter?.getData()?.isEmpty() ?: true
    }

    override fun isShowingPlaceholder() = empty?.visibility == View.VISIBLE || emptyError?.visibility == View.VISIBLE

    /**
     * @param v: View to add as header inside nestedscroll
     * @param width: width of your view (MATCH_PARENT, WRAP_CONTENT)
     * @param height: height of your view (MATCH_PARENT, WRAP_CONTENT)
     */
    fun addNestedHeaderView(v: View, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) {
        if (!wrapInNestedScroll) {
            IllegalStateException("Function only available when using attr wrapInNestedScroll with true")
        }
        findViewById<LinearLayout>(R.id.nestedHeader)?.let {
            it.visibility = View.VISIBLE
            v.layoutParams = ViewGroup.LayoutParams(width, height)
            it.addView(v)
        }
    }

    fun release(){
        adapter?.release()

        val itemCount = recyclerView?.adapter?.itemCount ?: 0
        for (i in 0..itemCount) {
            (recyclerView?.findViewHolderForAdapterPosition(i) as? ViewHolder<*>)?.onDestroy()
        }
        recyclerView?.adapter = null
        recyclerView = null
        dataSetChangedListener = null
    }

    /**
     * @param v: View to add as footer inside nestedscroll
     * @param width: width of your view (MATCH_PARENT, WRAP_CONTENT)
     * @param height: height of your view (MATCH_PARENT, WRAP_CONTENT)
     */
    fun addNestedFooterView(v: View, width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) {
        if (!wrapInNestedScroll) {
            IllegalStateException("Function only available when using attr wrapInNestedScroll with true")
        }
        findViewById<LinearLayout>(R.id.nestedFooter)?.let {
            it.visibility = View.VISIBLE
            v.layoutParams = ViewGroup.LayoutParams(width, height)
            it.addView(v)
        }
    }

    override fun hideHeaders() {
        recyclerView?.visibility = View.GONE
    }

    fun isListAtTop(): Boolean {
        return (layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() == 0
    }
}