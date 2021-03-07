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
    private val onItemClick: (SiteInfo, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SITE = 0
        private const val TYPE_INTERVAL = 1
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

    fun onItemMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        val intervalPosition = data.siteCount
        when {
            (fromPosition < intervalPosition
                    && toPosition < intervalPosition) -> {
                data.swap(fromPosition, toPosition)
            }
            (fromPosition > intervalPosition
                    && toPosition > intervalPosition) -> {
                val offset = intervalPosition + 1
                data.swapDisable(fromPosition - offset, toPosition - offset)
            }
            (fromPosition == intervalPosition) -> {
                if (toPosition < intervalPosition) {
                    val info = data.removeAt(toPosition)
                    data.addDisable(0, info)
                } else {
                    val info = data.removeDisableAt(0)
                    data.add(fromPosition, info)
                }
            }
            (toPosition == intervalPosition) -> {
                if (fromPosition < intervalPosition) {
                    val info = data.removeAt(fromPosition)
                    data.addDisable(0, info)
                } else {
                    val info = data.removeDisableAt(0)
                    data.add(toPosition, info)
                }
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder, direction: ListTouchHelper.Direction) {
        val position = viewHolder.adapterPosition
        if (position < data.siteCount) {
            data.removeAt(position)
        } else {
            data.removeDisableAt(position - data.siteCount - 1)
        }
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_INTERVAL) {
            return SiteIntervalHolder.create(parent)
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
        return data.siteCount + data.disableCount + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.siteCount -> {
                TYPE_INTERVAL
            }
            else -> {
                TYPE_SITE
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