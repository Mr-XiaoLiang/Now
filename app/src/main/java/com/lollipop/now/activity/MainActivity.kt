package com.lollipop.now.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.data.SiteHelper
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.ListTouchHelper
import com.lollipop.now.service.FloatingService
import com.lollipop.now.ui.EditDialog
import com.lollipop.now.ui.SiteAdapter
import com.lollipop.now.util.doAsync
import com.lollipop.now.util.onUI
import com.lollipop.now.util.setMargin
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private var editDialog: EditDialog? = null

    private val siteHelper = SiteHelper()
    private val siteAdapter = SiteAdapter(siteHelper, ::changeItem)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initWindowFlag()
        initRootGroup(rootGroup)
        editDialog = EditDialog(dialogRoot, dialogBackground, dialogContent,
            dialogIconView, titleInputText, urlInputText,
            submitBtn, cancelBtn, ::onSiteInfoChange)

        recyclerView.adapter = siteAdapter
        recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false)
        ListTouchHelper.with(recyclerView)
                .moveOrientation(DirectionInfo.VERTICAL)
                .swipeOrientation(DirectionInfo.HORIZONTAL)
                .onMoved(siteAdapter::onItemMoved)
                .onSwiped(siteAdapter::onItemSwiped)
        siteAdapter.notifyDataSetChanged()

        startBtn.setOnClickListener {
            FloatingService.start(this, false)
        }

        netDelaySwitch.isChecked = SiteHelper.isNetDelay(this)
        netDelaySwitch.setOnCheckedChangeListener { _, isChecked ->
            SiteHelper.enableNetDelay(this, isChecked)
        }

        doAsync {
            siteHelper.read(this)
            onUI {
                siteAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addItem) {
            addItem()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addItem() {
        editDialog?.show(SiteInfo(), -1)
    }

    private fun changeItem(info: SiteInfo, index: Int) {
        editDialog?.show(info, index)
    }

    private fun onSiteInfoChange(name: String, url: String, tag: Int) {
        val info = SiteInfo(name, SiteHelper.fixUrl(url))
        if (tag < 0) {
            siteAdapter.addInfo(info)
        } else {
            siteAdapter.changeInfo(tag, info)
        }
    }

    override fun onWindowInsetsChange(left: Int, top: Int, right: Int, bottom: Int) {
        appBarLayout.setPadding(left, top, right, 0)
        startBtn.setMargin {
            val baseMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            it.bottomMargin = baseMargin + bottom
            it.rightMargin = baseMargin + right
        }
        dialogContent.setMargin {
            val baseMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            it.topMargin = baseMargin + top
            it.rightMargin = baseMargin + right
            it.leftMargin = baseMargin + left
        }
    }

    override fun onBackPressed() {
        if (editDialog?.isShown == true) {
            editDialog?.dismiss()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        siteHelper.destroy()
    }

}