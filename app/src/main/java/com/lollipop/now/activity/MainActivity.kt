package com.lollipop.now.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.data.SiteHelper
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.databinding.ActivityMainBinding
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.ListTouchHelper
import com.lollipop.now.service.FloatingService
import com.lollipop.now.ui.EditDialog
import com.lollipop.now.ui.SiteAdapter
import com.lollipop.now.util.SharedPreferencesUtils.privateGet
import com.lollipop.now.util.SharedPreferencesUtils.privateSet
import com.lollipop.now.util.doAsync
import com.lollipop.now.util.lazyBind
import com.lollipop.now.util.onUI
import com.lollipop.now.util.setMargin

class MainActivity : BaseActivity() {

    companion object {
        private const val KEY_NOTIFICATION_ALERT = "KEY_NOTIFICATION_ALERT"
    }

    private var editDialog: EditDialog? = null

    private val siteHelper = SiteHelper()
    private val siteAdapter = SiteAdapter(siteHelper, ::changeItem)

    private val binding: ActivityMainBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initWindowFlag()
        initRootGroup(binding.rootGroup)
        editDialog = EditDialog(
            binding.dialogRoot,
            binding.dialogBackground,
            binding.dialogContent,
            binding.dialogIconView,
            binding.titleInputText,
            binding.urlInputText,
            binding.submitBtn,
            binding.cancelBtn, ::onSiteInfoChange
        )

        binding.recyclerView.adapter = siteAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        ListTouchHelper.with(binding.recyclerView)
            .moveOrientation(DirectionInfo.VERTICAL)
            .swipeOrientation(DirectionInfo.HORIZONTAL)
            .onMoved(siteAdapter::onItemMoved)
            .onSwiped(siteAdapter::onItemSwiped)
        siteAdapter.notifyDataSetChanged()

        binding.startBtn.setOnClickListener {
            FloatingService.start(this, false)
        }

        binding.netDelaySwitch.isChecked = SiteHelper.isNetDelay(this)
        binding.netDelaySwitch.setOnCheckedChangeListener { _, isChecked ->
            SiteHelper.enableNetDelay(this, isChecked)
        }

        if (!privateGet(KEY_NOTIFICATION_ALERT, false)) {
            showNotificationAlert()
        }

    }

    private fun showNotificationAlert() {
        AlertDialog.Builder(this)
            .setTitle(R.string.hint_title_permission)
            .setMessage(R.string.hint_message_permission)
            .setPositiveButton(R.string.complete) { dialog, _ ->
                dialog.dismiss()
            }.setNeutralButton(R.string.remember) { dialog, _ ->
                privateSet(KEY_NOTIFICATION_ALERT, true)
                dialog.dismiss()
            }.show()
    }

    override fun onResume() {
        super.onResume()
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
        return when (item.itemId) {
            R.id.addItem -> {
                addItem()
                true
            }
            R.id.copyItem -> {
                startActivity(Intent(this, CopyActivity::class.java))
                true
            }
            R.id.notificationHelper -> {
                showNotificationAlert()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
        binding.appBarLayout.setPadding(left, top, right, 0)
        binding.startBtn.setMargin {
            val baseMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            it.bottomMargin = baseMargin + bottom
            it.rightMargin = baseMargin + right
        }
        binding.dialogContent.setMargin {
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