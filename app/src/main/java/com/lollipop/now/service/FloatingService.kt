package com.lollipop.now.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.task
import com.lollipop.now.R
import com.lollipop.now.data.OffsetInfo
import com.lollipop.now.data.SiteHelper
import com.lollipop.now.databinding.FloatingItemBinding
import com.lollipop.now.ui.FloatingViewHelper
import java.util.LinkedList
import java.util.TimeZone

class FloatingService : Service() {

    companion object {

        private const val FOLLOWERS_CHANNEL_ID = "liang.lollipop.now.Floating"

        //消息通知的ID
        private const val NOTIFICATION_ID = 548

        private const val SYNC_DELAY = 1000L

        // 请求权限的pendingIntent
        private const val PENDING_REQUEST_PERMISSION = 23333

        // 关闭悬浮窗
        private const val ACTION_CLOSE_FLOATING = "lollipop.now.CLOSE_FLOATING"

        private const val ARG_STOP = "ARG_CLOSE_FLOATING"

        fun start(context: Context, isStop: Boolean) {
            val intent = Intent(context, FloatingService::class.java)
            intent.putExtra(ARG_STOP, isStop)
            if (!isStop) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

    }

    private val siteHelper = SiteHelper()

    private var isReady = false

    private lateinit var notificationManager: NotificationManager
    private val floatingHolderList = ArrayList<ViewHolder>()
    private val recyclerList = LinkedList<ViewHolder>()
    private val floatingViewHelper: FloatingViewHelper by lazy {
        FloatingViewHelper.create(getSystemService(Context.WINDOW_SERVICE) as WindowManager)
    }

    private val closeBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CLOSE_FLOATING) {
                closeAllFloating()
            }
        }
    }

    private val syncNetTime = task {
        siteHelper.async()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        siteHelper.onSync {
            checkFloating()
            startSync()
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannels()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                closeBroadcast,
                IntentFilter(ACTION_CLOSE_FLOATING),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                closeBroadcast,
                IntentFilter(ACTION_CLOSE_FLOATING)
            )
        }
    }

    private fun createChannels() {
        val androidChannel = NotificationChannel(
            FOLLOWERS_CHANNEL_ID,
            getString(R.string.floating_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        androidChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        androidChannel.importance = NotificationManager.IMPORTANCE_NONE
        notificationManager.createNotificationChannel(androidChannel)
    }

    private fun createNotification(): Notification {
        val closeIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID,
            Intent(ACTION_CLOSE_FLOATING),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        val builder = NotificationCompat.Builder(this, FOLLOWERS_CHANNEL_ID)
        builder.setAutoCancel(false)//可以点击通知栏的删除按钮删除
            .setPriority(NotificationCompat.PRIORITY_MIN)//最高等级的通知优先级
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)//设置锁屏的显示模式，此处为保护模式
            .setWhen(System.currentTimeMillis())//设置消息时间
            .setSmallIcon(R.drawable.ic_small_logo)//设置小图标
            .setOngoing(true)
            .setShowWhen(false)
            .setContentTitle(getString(R.string.floating_notif_title))//设置标题
            .setContentText(getString(R.string.floating_notif_msg))//设置内容
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            ) // 设置下拉列表中的图标(大图标)
            .addAction(0, getString(R.string.close_floating), closeIntent)

        return builder.build()
    }

    private fun checkPermission(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            notificationToOpenPermission()
            stop()
            return false
        }
        return true
    }

    private fun notificationToOpenPermission() {
        val intent = PendingIntent.getActivity(
            this,
            PENDING_REQUEST_PERMISSION,
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        val notification = NotificationCompat.Builder(this, FOLLOWERS_CHANNEL_ID)
            .setContentTitle(getString(R.string.notifi_title_no_alert))
            .setContentText(getString(R.string.notifi_msg_no_alert))
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            ) // 设置下拉列表中的图标(大图标)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)//最高等级的通知优先级
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_small_logo)
            .setFullScreenIntent(intent, true)
            .setContentIntent(intent)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showForeground()
        if (!checkPermission() || intent == null) {
            stop()
            return START_REDELIVER_INTENT
        }
        val isStop = intent.getBooleanExtra(ARG_STOP, false)
        if (isStop) {
            stop()
            return START_REDELIVER_INTENT
        }
        isReady = true
        siteHelper.read(this)
        checkFloating()
        startSync()
        return START_REDELIVER_INTENT
    }

    private fun startSync() {
        if (!isReady) {
            return
        }
        syncNetTime.cancel()
        syncNetTime.delay(SYNC_DELAY)
    }

    private fun showForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
            )
        }
    }

    private fun checkFloating() {
        while (floatingHolderList.isNotEmpty()
            && floatingHolderList.size > siteHelper.siteCount
        ) {
            removeHolder(floatingHolderList[0])
        }
        while (floatingHolderList.size < siteHelper.siteCount) {
            createHolder()
        }
        for (index in 0 until siteHelper.siteCount) {
            floatingHolderList[index].onStart(siteHelper.getOffset(index))
        }
    }

    private fun removeView(view: View) {
        try {
            floatingViewHelper.removeView(view)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun closeAllFloating() {
        for (holder in floatingHolderList) {
            holder.onStop()
            removeView(holder.view)
        }
        floatingHolderList.clear()
        notificationManager.cancel(NOTIFICATION_ID)
        stop()
        System.gc()
    }

    private fun createHolder(): ViewHolder {
        val holder = if (recyclerList.isNotEmpty()) {
            recyclerList.removeFirst()
        } else {
            ViewHolder(this)
        }
        floatingHolderList.add(holder)
        addView(holder.view)
        return holder
    }

    private fun removeHolder(holder: ViewHolder) {
        holder.onStop()
        removeView(holder.view)
        floatingHolderList.remove(holder)
        recyclerList.add(holder)
    }

    private fun addView(view: View) {
        if (!isReady) {
            return
        }
        try {
            floatingViewHelper.addView(view)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isReady = false
        siteHelper.destroy()
        recyclerList.clear()
        floatingHolderList.clear()
        unregisterReceiver(closeBroadcast)
    }

    private class ViewHolder(context: Context) {

        companion object {
            private const val SECONDS = 1000L
            private const val MINUTES = SECONDS * 60
            private const val HOURS = MINUTES * 60
            private const val DAY = HOURS * 24
        }

        var offsetInfo: OffsetInfo? = null
            private set

        private val log by lazyLogD()

        private val viewBinding: FloatingItemBinding = LayoutInflater.from(context).bind()

        val view: View
            get() {
                return viewBinding.root
            }

        val localOffset = TimeZone.getDefault().rawOffset

        private val tempBuilder = StringBuilder()

        private val updateTask = task {
            onTimeChange()
        }

        init {
            viewBinding.timeView.typeface =
                Typeface.createFromAsset(context.assets, "DroidSansMono.ttf")
        }

        fun onStart(info: OffsetInfo) {
            offsetInfo = info
            viewBinding.iconView.text = info.name
            onTimeChange()
            log("onStart: " + info.name + ", offset: " + info.offset)
        }

        fun onStop() {
            offsetInfo = null
            updateTask.cancel()
        }

        private fun onTimeChange() {
            viewBinding.timeView.text = getTime()
            updateTask.cancel()
            updateTask.sync()
        }

        private fun getTime(): String {
            val offset = offsetInfo?.offset ?: 0L
            if (offset == SiteHelper.OFFSET_ERROR) {
                return viewBinding.timeView.resources.getString(R.string.sync_error)
            }
            val now = System.currentTimeMillis() + offset + localOffset
            val inDay = now % DAY
            val hours = (inDay / HOURS).toString().fixLength(2)
            val minutes = (inDay % HOURS / MINUTES).toString().fixLength(2)
            val seconds = (inDay % MINUTES / SECONDS).toString().fixLength(2)
            val ms = (inDay % SECONDS).toString().fixLength(3)
            val builder = tempBuilder
            builder.clear()
            builder.append(hours)
            builder.append(":")
            builder.append(minutes)
            builder.append(":")
            builder.append(seconds)
            builder.append(".")
            builder.append(ms)
            return builder.toString()
        }

        private fun String.fixLength(length: Int): String {
            val builder = tempBuilder
            builder.clear()
            builder.append(this)
            while (builder.length < length) {
                builder.insert(0, "0")
            }
            return builder.toString()
        }

    }

}