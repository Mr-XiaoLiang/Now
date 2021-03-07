package com.lollipop.now.data

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.SparseArray
import androidx.core.util.set
import com.lollipop.now.util.CommonUtil
import com.lollipop.now.util.SharedPreferencesUtils.get
import com.lollipop.now.util.SharedPreferencesUtils.set
import com.lollipop.now.util.createTask
import com.lollipop.now.util.doAsync
import com.lollipop.now.util.onUI
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 11/12/20 07:57
 * 站点辅助器
 */
class SiteHelper {

    companion object {

        const val OFFSET_ERROR = Long.MAX_VALUE

        private const val KEY_SITE_INFO = "SITE_INFO"

        private const val KEY_DISABLE_INFO = "DISABLE_INFO"

        private const val KEY_NET_DELAY = "NET_DELAY"

        private const val KEY_NAME = "name"
        private const val KEY_SITE = "url"

        private const val HTTP = "http://"
        private const val HTTPS = "https://"

        private var DEFAULT_SITE_INFO = ""

        private const val APPLY_DELAY = 500L

        private val EMPTY_SITE = SiteInfo("", "")
        private val EMPTY_OFFSET = OffsetInfo("", "", 0L)

        private fun encode(value: String): String {
            return Base64.encodeToString(value.toByteArray(Charsets.UTF_8),
                Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
        }

        private fun decode(value: String): String {
            return String(Base64.decode(value,
                Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE))
        }

        fun enableNetDelay(context: Context, isEnable: Boolean) {
            context[KEY_NET_DELAY] = isEnable
        }

        fun isNetDelay(context: Context): Boolean {
            return context[KEY_NET_DELAY, true]
        }

        fun fixUrl(url: String): String {
            if (url.startsWith(HTTP, true)) {
                return HTTPS + url.substring(HTTP.length)
            }
            if (url.startsWith(HTTPS, true)) {
                return url
            }
            return HTTPS + url
        }

        fun readDefaultInfo(context: Context): String {
            if (DEFAULT_SITE_INFO.isBlank()) {
                val open = context.assets.open("default.json")
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length = open.read(buffer)
                while (length >= 0) {
                    out.write(buffer, 0, length)
                    length = open.read(buffer)
                }
                DEFAULT_SITE_INFO = out.toString(Charsets.UTF_8.displayName())
                DEFAULT_SITE_INFO = encode(DEFAULT_SITE_INFO)
            }
            return DEFAULT_SITE_INFO
        }

        fun stringToSiteList(value: String, list: ArrayList<SiteInfo>) {
            list.clear()
            val jsonArray = JSONArray(decode(value))
            for (index in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(index) ?: continue
                val name = obj.optString(KEY_NAME)?:""
                val site = obj.optString(KEY_SITE)?:""
                list.add(SiteInfo(name, site))
            }
        }

        fun siteListToString(list: ArrayList<SiteInfo>): String {
            val jsonArray = JSONArray()
            for (info in list) {
                val obj = JSONObject()
                obj.put(KEY_NAME, info.name)
                obj.put(KEY_SITE, info.url)
                jsonArray.put(obj)
            }
            return encode(jsonArray.toString())
        }

        fun getSiteInfo(context: Context): String {
            return context[KEY_SITE_INFO, ""]
        }

    }

    private var context: Context? = null

    private var applyLock = false
    private var isDestroy = false
    private var pendingSync = false

    private var isUseNetDelay = true

    private val siteList = ArrayList<SiteInfo>()

    private val disableSiteList = ArrayList<SiteInfo>()

    private val offsetList = SparseArray<OffsetInfo>()

    private val applyTempList = ArrayList<SiteInfo>()
    private val syncTempList = ArrayList<SiteInfo>()

    private var onSyncCallback: ((Boolean) -> Unit)? = null

    private val applyChangeTask = createTask {
        if (applyLock) {
            return@createTask
        }
        applyLock = true
        onSyncCallback?.invoke(true)
        doAsync {
            applyTempList.clear()
            applyTempList.addAll(siteList)
            val siteListValue = siteListToString(applyTempList)
            applyTempList.clear()
            if (isDestroy) {
                return@doAsync
            }
            context?.set(KEY_SITE_INFO, siteListValue)

            applyTempList.clear()
            applyTempList.addAll(disableSiteList)
            val disableListValue = siteListToString(applyTempList)
            applyTempList.clear()
            if (isDestroy) {
                return@doAsync
            }
            context?.set(KEY_DISABLE_INFO, disableListValue)

            applyLock = false
            onUI {
                onSyncCallback?.invoke(false)
                if (pendingSync) {
                    pendingSync = false
                    applyChange()
                }
            }
        }
    }

    private val netSyncTask = createTask {
        onUI {
            onSyncCallback?.invoke(true)
        }
        var name: String
        var url: String
        var offset: Long
        syncTempList.clear()
        syncTempList.addAll(siteList)
        for (index in syncTempList.indices) {
            if (isDestroy) {
                return@createTask
            }
            val site = syncTempList[index]
            name = site.name
            url = site.url
            try {
                val start = System.currentTimeMillis()
                val connection = URL(url).openConnection() as HttpURLConnection //生成连接对象
                connection.requestMethod = "HEAD"
                connection.useCaches = false
                connection.connect() //发出连接
                var date = connection.date //取得网站日期时间
                val ageField = connection.getHeaderField("Age")
                if (!TextUtils.isEmpty(ageField)) {
                    try {
                        val age = ageField.toLong()
                        date += age
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                val end = System.currentTimeMillis()
                offset = date - end
                if (isUseNetDelay) {
                    offset += ((end - start) / 2)
                }
            } catch (e: Throwable) {
                offset = OFFSET_ERROR
                e.printStackTrace()
            }
            offsetList[index] = OffsetInfo(name, url, offset)
        }
        onUI {
            onSyncCallback?.invoke(false)
        }
    }

    val siteCount: Int
        get() {
            return siteList.size
        }

    val disableCount: Int
        get() {
            return disableSiteList.size
        }

    fun getSite(index: Int): SiteInfo {
        if (siteList.isEmpty() || index < 0 || index >= siteList.size) {
            return EMPTY_SITE
        }
        return siteList[index]
    }

    fun getDisableSite(index: Int): SiteInfo {
        if (disableSiteList.isEmpty() || index < 0 || index >= disableSiteList.size) {
            return EMPTY_SITE
        }
        return disableSiteList[index]
    }

    fun getOffset(index: Int): OffsetInfo {
        if (siteList.isEmpty() || index < 0 || index >= siteList.size) {
            return EMPTY_OFFSET
        }
        return offsetList[index]?:EMPTY_OFFSET
    }

    fun read(context: Context) {
        this.context = context
        siteList.clear()
        isUseNetDelay = isNetDelay(context)
        try {
            var value = getSiteInfo(context)
            if (value.isBlank()) {
                value = readDefaultInfo(context)
            }
            stringToSiteList(value, siteList)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        disableSiteList.clear()
        try {
            val value = context[KEY_DISABLE_INFO, ""]
            stringToSiteList(value, disableSiteList)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun add(infoListL: List<SiteInfo>) {
        siteList.addAll(infoListL)
        applyChange()
    }

    fun add(index: Int, siteInfo: SiteInfo) {
        siteList.add(index, siteInfo)
        applyChange()
    }

    fun addDisable(index: Int, siteInfo: SiteInfo) {
        disableSiteList.add(index, siteInfo)
        applyChange()
    }

    fun set(index: Int, siteInfo: SiteInfo) {
        siteList[index] = siteInfo
        applyChange()
    }

    fun setDisableSite(index: Int, info: SiteInfo) {
        disableSiteList[index] = info
        applyChange()
    }

    fun onSync(callback: (Boolean) -> Unit) {
        this.onSyncCallback = callback
    }

    fun removeAt(index: Int): SiteInfo {
        val removeAt = siteList.removeAt(index)
        applyChange()
        return removeAt
    }

    fun swap(from: Int, to: Int) {
        Collections.swap(siteList, from, to)
        applyChange()
    }

    fun swapDisable(from: Int, to: Int) {
        Collections.swap(disableSiteList, from, to)
        applyChange()
    }

    fun removeDisableAt(index: Int): SiteInfo {
        val removeAt = disableSiteList.removeAt(index)
        applyChange()
        return removeAt
    }

    private fun applyChange() {
        if (!applyLock) {
            CommonUtil.remove(applyChangeTask)
            CommonUtil.delay(APPLY_DELAY, applyChangeTask)
        } else {
            pendingSync = true
        }
        clearCache()
    }

    private fun clearCache() {
        offsetList.clear()
    }

    fun sync() {
        CommonUtil.doAsync(netSyncTask)
    }

    fun destroy() {
        context = null
        CommonUtil.remove(applyChangeTask)
        applyLock = true
    }

}