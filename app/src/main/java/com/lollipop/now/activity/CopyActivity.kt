package com.lollipop.now.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.data.SiteHelper
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.ui.ImportSiteAdapter
import com.lollipop.now.util.*
import kotlinx.android.synthetic.main.activity_copy.*


class CopyActivity : BaseActivity() {

    private val siteList = ArrayList<SiteInfo>()
    private val selectedList = ArrayList<SiteInfo>()
    private val siteHelper = SiteHelper()
    private val siteAdapter = ImportSiteAdapter(siteList, ::isSelected, ::onSiteClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copy)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initWindowFlag()
        initRootGroup(rootGroup)
        initView()
        siteHelper.onSync {
            if (it) {
                loadingView.show()
            } else {
                loadingView.hide()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_import, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.shareItem) {
            share()
            return true
        }
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        defaultBtn.setOnClickListener {
            siteInputText.setText(SiteHelper.readDefaultInfo(this))
        }
        onSiteUpdate(false)
        parseBtn.setOnClickListener {
            parseInfo()
        }
        selectAllBtn.setOnClickListener {
            if (siteList.size == selectedList.size) {
                selectedList.clear()
            } else {
                selectedList.clear()
                selectedList.addAll(siteList)
            }
            siteAdapter.notifyDataSetChanged()
        }
        importBtn.setOnClickListener {
            import()
        }
        recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        recyclerView.adapter = siteAdapter
        siteAdapter.notifyDataSetChanged()
    }

    private fun isSelected(info: SiteInfo): Boolean {
        return selectedList.contains(info)
    }

    private fun onSiteClick(position: Int) {
        val siteInfo = siteList[position]
        if (!selectedList.remove(siteInfo)) {
            selectedList.add(siteInfo)
        }
        importBtn.visibility = if (selectedList.isEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        siteAdapter.notifyItemChanged(position)
    }

    private fun parseInfo() {
        loadingView.show()
        siteInputText.error = null
        siteList.clear()
        selectedList.clear()
        doAsync({
            onUI {
                loadingView.hide()
                onSiteUpdate(true)
                siteInputText.error = getString(R.string.parse_error)
            }
        }) {
            val siteInfo = siteInputText.text.toString()
            SiteHelper.stringToSiteList(siteInfo, siteList)
            onUI {
                loadingView.hide()
                onSiteUpdate(true)
            }
        }
    }

    private fun share() {
        var siteInfo = SiteHelper.getSiteInfo(this)
        if (TextUtils.isEmpty(siteInfo)) {
            siteInfo = SiteHelper.readDefaultInfo(this)
        }
        //获取剪贴板管理器：
        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val clipData = ClipData.newPlainText("siteInfo", siteInfo)
        // 将ClipData内容放到系统剪贴板里。
        manager.setPrimaryClip(clipData)
        Toast.makeText(this, R.string.copy_done, Toast.LENGTH_SHORT).show()
    }

    private fun import() {
        if (selectedList.isEmpty()) {
            return
        }
        siteHelper.read(this)
        siteHelper.add(selectedList)
        Toast.makeText(this, R.string.import_done, Toast.LENGTH_SHORT).show()
    }

    private fun onSiteUpdate(isAnimation: Boolean = true) {
        siteAdapter.notifyDataSetChanged()
        if (!isAnimation) {
            if (siteList.isEmpty()) {
                cardRoot.translationZ = 0F
                cardContentGroup.visibility = View.INVISIBLE
            } else {
                val cardElevation = resources.getDimensionPixelSize(R.dimen.cardElevation)
                cardRoot.translationZ = cardElevation * 1F
                cardContentGroup.visibility = View.VISIBLE
            }
        } else {
            if (siteList.isEmpty()) {
                cardRoot.animate().apply {
                    cancel()
                    translationZ(0F)
                    start()
                }
                cardContentGroup.animate().apply {
                    cancel()
                    alpha(0F)
                    lifecycleBinding {
                        onEnd {
                            cardContentGroup.visibility = View.INVISIBLE
                            removeThis(it)
                        }
                    }
                    start()
                }
            } else {
                cardRoot.animate().apply {
                    cancel()
                    val cardElevation = resources.getDimensionPixelSize(R.dimen.cardElevation)
                    translationZ(cardElevation * 1F)
                    start()
                }
                cardContentGroup.animate().apply {
                    cancel()
                    alpha(1F)
                    lifecycleBinding {
                        onStart {
                            cardContentGroup.visibility = View.VISIBLE
                            removeThis(it)
                        }
                    }
                    start()
                }
            }
        }
        importBtn.visibility = if (selectedList.isEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    override fun onWindowInsetsChange(left: Int, top: Int, right: Int, bottom: Int) {
        super.onWindowInsetsChange(left, top, right, bottom)
        appBarLayout.setPadding(left, top, right, 0)
        rootGroup.setPadding(0, 0, 0, bottom)
    }

    override fun onDestroy() {
        super.onDestroy()
        siteHelper.destroy()
    }

}