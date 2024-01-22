package com.lollipop.now.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onUI
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

class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_NOTIFICATION_ALERT = "KEY_NOTIFICATION_ALERT"
    }

    private val editDialog: EditDialog by lazy {
        EditDialog(
            binding.dialogRoot,
            binding.dialogBackground,
            binding.dialogContent,
            binding.dialogIconView,
            binding.titleInputText,
            binding.urlInputText,
            binding.submitBtn,
            binding.cancelBtn,
            ::onSiteInfoChange
        ).apply {
            setShownStatusChangeListener {
                editDialogBackPressHandler.isEnabled = it
            }
        }
    }

    private val siteHelper = SiteHelper()
    private val siteAdapter = SiteAdapter(siteHelper, ::changeItem)

    private val binding: ActivityMainBinding by lazyBind()

    private val editDialogBackPressHandler = BackPressHandler.create(false) {
        editDialog.dismiss()
        it.isEnabled = false
    }

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        WindowInsetsHelper.fitsSystemWindows(this)
        binding.appBarLayout.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        binding.startBtn.fixInsetsByMargin(WindowInsetsEdge.CONTENT)
        binding.dialogContent.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        BackPressHandler.findDispatcher(this).bind(editDialogBackPressHandler)
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
            if (!notificationManager.areNotificationsEnabled()) {
                hintNotificationDisableAlert()
            } else if (!Settings.canDrawOverlays(this)) {
                hintFloatingWindowAlert()
            } else {
                FloatingService.start(this, false)
            }
        }

        binding.netDelaySwitch.isChecked = SiteHelper.isNetDelay(this)
        binding.netDelaySwitch.setOnCheckedChangeListener { _, isChecked ->
            SiteHelper.enableNetDelay(this, isChecked)
        }

        if (!privateGet(KEY_NOTIFICATION_ALERT, false)) {
            showNotificationAlert()
        }

    }

    private fun hintFloatingWindowAlert() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notifi_title_no_alert)
            .setMessage(R.string.notifi_msg_no_alert)
            .setPositiveButton(R.string.complete) { dialog, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
                dialog.dismiss()
            }.show()
    }

    private fun hintNotificationDisableAlert() {
        AlertDialog.Builder(this)
            .setTitle(R.string.hint_title_permission)
            .setMessage(R.string.hint_notification_disable)
            .setPositiveButton(R.string.complete) { dialog, _ ->
                startActivity(
                    Intent().apply {
                        setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
                    }
                )
                dialog.dismiss()
            }.show()
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

    @SuppressLint("NotifyDataSetChanged")
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
        editDialog.show(SiteInfo(), -1)
    }

    private fun changeItem(info: SiteInfo, index: Int) {
        editDialog.show(info, index)
    }

    private fun onSiteInfoChange(name: String, url: String, tag: Int) {
        val info = SiteInfo(name, SiteHelper.fixUrl(url))
        if (tag < 0) {
            siteAdapter.addInfo(info)
        } else {
            siteAdapter.changeInfo(tag, info)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        siteHelper.destroy()
    }

}