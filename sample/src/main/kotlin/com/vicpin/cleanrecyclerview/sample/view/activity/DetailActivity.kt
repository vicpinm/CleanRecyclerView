package com.vicpin.cleanrecyclerview.sample.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import butterknife.ButterKnife
import com.vicpin.cleanrecyclerview.sample.R
import com.vicpin.cleanrecyclerview.sample.extensions.loadWithPicasso
import com.vicpin.cleanrecyclerview.sample.extensions.startActivityWithTransition
import com.vicpin.cleanrecyclerview.sample.model.Item
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {


    companion object {

        private val ITEM_EXTRA = "item_extra"

        fun launchActivity(act: Activity, sharedView: View, item: Item) {
            val i = Intent(act, DetailActivity::class.java)
            i.putExtra(ITEM_EXTRA, item)
            act.startActivityWithTransition(i, sharedView, R.string.transition_item)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        ButterKnife.bind(this)

        initView()
    }

    fun initView(){
        if(intent.hasExtra(ITEM_EXTRA)){
            val item = intent.getSerializableExtra(ITEM_EXTRA) as Item
            itemtitle.text = item.title
            image.loadWithPicasso(item.imageUrl)
            description.text = item.description
        }
    }

}
