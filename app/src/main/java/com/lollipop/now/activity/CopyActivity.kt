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
import com.lollipop.now.databinding.ActivityCopyBinding
import com.lollipop.now.ui.ImportSiteAdapter
import com.lollipop.now.util.*


class CopyActivity : BaseActivity() {

    private val siteList = ArrayList<SiteInfo>()
    private val selectedList = ArrayList<SiteInfo>()
    private val siteHelper = SiteHelper()
    private val siteAdapter = ImportSiteAdapter(siteList, ::isSelected, ::onSiteClick)

    private val binding: ActivityCopyBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initWindowFlag()
        initRootGroup(binding.rootGroup)
        initView()
        siteHelper.onSync {
            if (it) {
                binding.loadingView.show()
            } else {
                binding.loadingView.hide()
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
        binding.defaultBtn.setOnClickListener {
            binding.siteInputText.setText(SiteHelper.readDefaultInfo(this))
        }
        onSiteUpdate(false)
        binding.parseBtn.setOnClickListener {
            parseInfo()
        }
        binding.selectAllBtn.setOnClickListener {
            if (siteList.size == selectedList.size) {
                selectedList.clear()
            } else {
                selectedList.clear()
                selectedList.addAll(siteList)
            }
            onSelectedSiteChange()
            siteAdapter.notifyDataSetChanged()
        }
        binding.importBtn.setOnClickListener {
            import()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        binding.recyclerView.adapter = siteAdapter
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
        onSelectedSiteChange()
        siteAdapter.notifyItemChanged(position)
    }

    private fun onSelectedSiteChange() {
        binding.importBtn.visibility = if (selectedList.isEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    private fun parseInfo() {
        binding.loadingView.show()
        binding.siteInputText.error = null
        siteList.clear()
        selectedList.clear()
        doAsync({
            onUI {
                binding.loadingView.hide()
                onSiteUpdate(true)
                binding.siteInputText.error = getString(R.string.parse_error)
            }
        }) {
            val siteInfo = binding.siteInputText.text.toString()
            SiteHelper.stringToSiteList(siteInfo, siteList)
            onUI {
                binding.loadingView.hide()
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
                binding.cardRoot.translationZ = 0F
                binding.cardContentGroup.visibility = View.INVISIBLE
            } else {
                val cardElevation = resources.getDimensionPixelSize(R.dimen.cardElevation)
                binding.cardRoot.translationZ = cardElevation * 1F
                binding.cardContentGroup.visibility = View.VISIBLE
            }
        } else {
            if (siteList.isEmpty()) {
                binding.cardRoot.animate().apply {
                    cancel()
                    translationZ(0F)
                    start()
                }
                binding.cardContentGroup.animate().apply {
                    cancel()
                    alpha(0F)
                    lifecycleBinding {
                        onEnd {
                            binding.cardContentGroup.visibility = View.INVISIBLE
                            removeThis(it)
                        }
                    }
                    start()
                }
            } else {
                binding.cardRoot.animate().apply {
                    cancel()
                    val cardElevation = resources.getDimensionPixelSize(R.dimen.cardElevation)
                    translationZ(cardElevation * 1F)
                    start()
                }
                binding.cardContentGroup.animate().apply {
                    cancel()
                    alpha(1F)
                    lifecycleBinding {
                        onStart {
                            binding.cardContentGroup.visibility = View.VISIBLE
                            removeThis(it)
                        }
                    }
                    start()
                }
            }
        }
        onSelectedSiteChange()
    }

    override fun onWindowInsetsChange(left: Int, top: Int, right: Int, bottom: Int) {
        super.onWindowInsetsChange(left, top, right, bottom)
        binding.appBarLayout.setPadding(left, top, right, 0)
        binding.rootGroup.setPadding(0, 0, 0, bottom)
    }

    override fun onDestroy() {
        super.onDestroy()
        siteHelper.destroy()
    }

}