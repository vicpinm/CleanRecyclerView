package com.vicpin.cleanrecyclerview.sample.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.ubox.app.pagedrecyclerview.CleanRecyclerView
import com.vicpin.cleanrecyclerview.sample.R
import com.vicpin.cleanrecyclerview.sample.data.ItemCache
import com.vicpin.cleanrecyclerview.sample.data.ItemService
import com.vicpin.cleanrecyclerview.sample.model.Item
import com.vicpin.cleanrecyclerview.sample.view.adapter.AdapterItemView
import com.vicpin.presenteradapter.SimplePresenterAdapter
import com.vicpin.presenteradapter.listeners.ItemClickListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initList()
    }

    fun initList() {
        val cleanRecyclerView = list as CleanRecyclerView<Item>
        val adapter = SimplePresenterAdapter.with(AdapterItemView::class.java).setLayout(R.layout.adapter_item)

        cleanRecyclerView.load(adapter, ItemService::class, ItemCache::class)

        cleanRecyclerView.onItemClick(ItemClickListener { item, view -> DetailActivity.launchActivity(this, view.itemView.findViewById(R.id.header), item) })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val cleanRecyclerView = list as CleanRecyclerView<Item>

        when(item?.itemId){
            R.id.vertical -> cleanRecyclerView.layoutManager = LinearLayoutManager(this)
            R.id.horizontal -> cleanRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            R.id.grid -> cleanRecyclerView.layoutManager = GridLayoutManager(this, 2)
        }

        return true
    }

}
