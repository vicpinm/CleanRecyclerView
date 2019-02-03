package com.vicpin.cleanrecycler.sample.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vicpin.cleanrecycler.sample.R
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)


        noPagedList.setOnClickListener { startActivity(Intent(this, NoPagedListActivity::class.java)) }
        pagedList.setOnClickListener { startActivity(Intent(this, PagedListActivity::class.java)) }
        mappedList.setOnClickListener { startActivity(Intent(this, MappedListActivity::class.java)) }
        annotatedDatasourcesList.setOnClickListener { startActivity(Intent(this, AnnotatedListActivity::class.java)) }

    }
}