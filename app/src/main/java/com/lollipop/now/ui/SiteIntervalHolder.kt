package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.now.databinding.ItemSiteIntervalBinding
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.MovableHolder
import com.lollipop.now.list.SwipeableHolder

/**
 * @author lollipop
 * @date 11/12/20 11:14
 */
class SiteIntervalHolder private constructor(viewBinding: ItemSiteIntervalBinding) :
    RecyclerView.ViewHolder(viewBinding.root),
    SwipeableHolder, MovableHolder {

    companion object {
        fun create(group: ViewGroup): SiteIntervalHolder {
            return SiteIntervalHolder(
                group.bind(attach = false)
            )
        }
    }

    override fun canSwipe(): DirectionInfo {
        return DirectionInfo.NONE
    }

    override fun canMove(): DirectionInfo {
        return DirectionInfo.VERTICAL
    }

}