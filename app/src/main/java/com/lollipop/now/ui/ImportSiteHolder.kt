package com.lollipop.now.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
class ImportSiteHolder
private constructor(
    view: View,
    private val onClick: (ImportSiteHolder) -> Unit): RecyclerView.ViewHolder(view) {

    companion object {
        fun create(group: ViewGroup,
                   onClick: (ImportSiteHolder) -> Unit): ImportSiteHolder {
            return ImportSiteHolder(
                LayoutInflater.from(group.context)
                    .inflate(R.layout.item_web_site, group, false),
                onClick)
        }
    }

    private val iconView: TextView = itemView.findViewById(R.id.iconView)
    private val titleView: TextView = itemView.findViewById(R.id.titleView)
    private val urlView: TextView = itemView.findViewById(R.id.urlView)
    private val selectedColor = ContextCompat.getColor(itemView.context, R.color.siteSelected)
    private val unselectedColor = ContextCompat.getColor(itemView.context, R.color.cardBackground)

    init {
        itemView.setOnClickListener {
            onClick(this)
        }
    }

    fun bind(info: SiteInfo, isSelect: Boolean) {
        iconView.text = info.name
        titleView.text = info.name
        urlView.text = info.url

        itemView.setBackgroundColor(if (isSelect) { selectedColor } else { unselectedColor })
    }

}