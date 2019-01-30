package com.vicpin.cleanrecycler.sample.view.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vicpin.cleanrecycler.sample.extensions.injector
import com.vicpin.cleanrecycler.sample.model.Item
import com.vicpin.cleanrecycler.sample.view.adapter.AdapterItemView
import com.vicpin.cleanrecycler.view.SimpleCleanRecyclerView
import com.vicpin.kpresenteradapter.SimplePresenterAdapter
import kotlinx.android.synthetic.main.activity_annotated.*
import com.vicpin.cleanrecycler.sample.R

class AnnotatedListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
        setContentView(R.layout.activity_annotated)
        initNoPagedList()
    }


    fun initNoPagedList() {
        val adapter = SimplePresenterAdapter(AdapterItemView::class, R.layout.adapter_item)
        list.load(adapter)
        list.onItemClick { item, view -> DetailActivity.launchActivity(this, view.itemView.findViewById(R.id.header), item) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.vertical -> list.layoutManager = LinearLayoutManager(this)
            R.id.horizontal -> list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            R.id.grid -> list.layoutManager = GridLayoutManager(this, 2)
        }

        return true
    }

}
