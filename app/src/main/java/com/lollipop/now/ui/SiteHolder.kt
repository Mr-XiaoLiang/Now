package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.databinding.ItemWebSiteBinding
import com.lollipop.now.util.bind
import com.lollipop.now.util.changeLayoutParams

/**
 * @author lollipop
 * @date 11/11/20 18:50
 * 站点的item
 */
class SiteHolder
private constructor(
    private val viewBinding: ItemWebSiteBinding,
    private val onClick: (SiteHolder) -> Unit): RecyclerView.ViewHolder(viewBinding.root) {

    companion object {
        fun create(group: ViewGroup,
                   onClick: (SiteHolder) -> Unit): SiteHolder {
            return SiteHolder(
                group.bind<ItemWebSiteBinding>()
                    .changeLayoutParams(matchWidth = true, matchHeight = false),
                onClick)
        }
    }

    init {
        itemView.setOnClickListener {
            onClick(this)
        }
    }

    fun bind(info: SiteInfo) {
        viewBinding.iconView.text = info.name
        viewBinding.titleView.text = info.name
        viewBinding.urlView.text = info.url
    }

}