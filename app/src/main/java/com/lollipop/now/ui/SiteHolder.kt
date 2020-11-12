package com.lollipop.now.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.MovableHolder

/**
 * @author lollipop
 * @date 11/11/20 18:50
 * 站点的item
 */
class SiteHolder
private constructor(
    view: View,
    private val onClick: (SiteHolder) -> Unit): RecyclerView.ViewHolder(view) {

    companion object {
        fun create(group: ViewGroup,
                   onClick: (SiteHolder) -> Unit): SiteHolder {
            return SiteHolder(
                LayoutInflater.from(group.context)
                    .inflate(R.layout.item_web_site, group, false),
                onClick)
        }
    }

    private val iconView: TextView = itemView.findViewById(R.id.iconView)
    private val titleView: TextView = itemView.findViewById(R.id.titleView)
    private val urlView: TextView = itemView.findViewById(R.id.urlView)

    init {
        itemView.setOnClickListener {
            onClick(this)
        }
    }

    fun bind(info: SiteInfo) {
        iconView.text = info.name
        titleView.text = info.name
        urlView.text = info.url
    }

}