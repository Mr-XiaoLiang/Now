package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.data.SiteInfo
import com.lollipop.now.databinding.ItemWebSiteBinding
import com.lollipop.now.util.bind
import com.lollipop.now.util.changeLayoutParams

/**
 * @author lollipop
 * @date 11/11/20 18:50
 * 站点的item
 */
class ImportSiteHolder
private constructor(
    private val viewBinding: ItemWebSiteBinding,
    private val onClick: (ImportSiteHolder) -> Unit): RecyclerView.ViewHolder(viewBinding.root) {

    companion object {
        fun create(group: ViewGroup,
                   onClick: (ImportSiteHolder) -> Unit): ImportSiteHolder {
            return ImportSiteHolder(
                group.bind<ItemWebSiteBinding>()
                    .changeLayoutParams(matchWidth = true, matchHeight = false),
                onClick)
        }
    }

    private val selectedColor = ContextCompat.getColor(itemView.context, R.color.siteSelected)
    private val unselectedColor = ContextCompat.getColor(itemView.context, R.color.cardBackground)

    init {
        itemView.setOnClickListener {
            onClick(this)
        }
    }

    fun bind(info: SiteInfo, isSelect: Boolean) {
        viewBinding.iconView.text = info.name
        viewBinding.titleView.text = info.name
        viewBinding.urlView.text = info.url

        itemView.setBackgroundColor(if (isSelect) { selectedColor } else { unselectedColor })
    }

}