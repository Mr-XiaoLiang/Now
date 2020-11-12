package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.data.SiteHelper
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.list.ListTouchHelper

/**
 * @author lollipop
 * @date 11/11/20 18:55
 */
class SiteAdapter(
        private val data: SiteHelper,
        private val onItemClick: (SiteInfo, Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SITE = 0
        private const val TYPE_INTERVAL = 1
        private const val TYPE_DISABLE = 2
        private const val TYPE_EMPTY = -1
    }

    private fun swap(fromPosition: Int, toPosition: Int) {
        val fromType = getItemViewType(fromPosition)
        val toType = getItemViewType(toPosition)
        val offset = data.siteCount + 1
        if (fromType != toType) {
            val fromInfo = removeInfo(fromPosition)
            if (fromInfo != null) {
                if (fromType == TYPE_SITE){
                    data.addDisable(0, fromInfo)
                } else if (fromType == TYPE_DISABLE){
                    data.add(data.siteCount, fromInfo)
                }
            }
        } else {
            if (fromType == TYPE_SITE) {
                data.swap(fromPosition, toPosition)
            } else if (fromType == TYPE_DISABLE) {
                data.swapDisable(fromPosition - offset, toPosition - offset)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun removeAt(position: Int) {
        val type = getItemViewType(position)
        val offset = data.siteCount + 1
        if (type == TYPE_SITE) {
            data.removeAt(position)
        } else {
            data.removeDisableAt(position - offset)
        }
        notifyItemRemoved(position)
    }

    private fun removeInfo(position: Int): SiteInfo? {
        val type = getItemViewType(position)
        val offset = data.siteCount + 1
        return when (type) {
            TYPE_SITE -> {
                if (position < 0
                    || position >= data.siteCount
                    || data.siteCount == 0) {
                    return null
                }
                data.removeAt(position)
            }
            TYPE_DISABLE -> {
                val index = position - offset
                if (index < 0
                    || index >= data.disableCount
                    || data.disableCount == 0) {
                    return null
                }
                data.removeDisableAt(index)
            }
            else -> {
                null
            }
        }
    }

    fun changeInfo(index: Int, info: SiteInfo) {
        if (index < data.siteCount) {
            data.set(index, info)
        } else {
            data.setDisableSite(index - data.siteCount - 1, info)
        }
        notifyItemChanged(index)
    }

    fun addInfo(info: SiteInfo) {
        data.add(0, info)
        notifyItemInserted(0)
    }

    fun onItemMoved(recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder): Boolean {
        swap(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder, direction: ListTouchHelper.Direction) {
        removeAt(viewHolder.adapterPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_INTERVAL) {
            return SiteIntervalHolder.create(parent)
        } else if (viewType == TYPE_EMPTY) {
            return EmptyHolder.create(parent)
        }
        return SiteHolder.create(parent, ::onSiteClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SiteHolder) {
            val info = if (position < data.siteCount) {
                data.getSite(position)
            } else {
                data.getDisableSite(position - data.siteCount - 1)
            }
            holder.bind(info)
        }
    }

    override fun getItemCount(): Int {
        return data.siteCount + data.disableCount + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == data.siteCount -> {
                TYPE_INTERVAL
            }
            position < data.siteCount -> {
                TYPE_SITE
            }
            position == (itemCount - 1) -> {
                TYPE_EMPTY
            }
            else -> {
                TYPE_DISABLE
            }
        }
    }

    private fun onSiteClick(holder: SiteHolder) {
        val adapterPosition = holder.adapterPosition
        if (getItemViewType(adapterPosition) == TYPE_INTERVAL) {
            return
        }
        val info = if (adapterPosition < data.siteCount) {
            data.getSite(adapterPosition)
        } else {
            data.getDisableSite(adapterPosition - data.siteCount - 1)
        }
        onItemClick(info, adapterPosition)
    }
}