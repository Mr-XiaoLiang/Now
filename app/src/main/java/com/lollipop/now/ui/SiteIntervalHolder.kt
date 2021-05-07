package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.databinding.ItemSiteIntervalBinding
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.MovableHolder
import com.lollipop.now.list.SwipeableHolder
import com.lollipop.now.util.bind
import com.lollipop.now.util.changeLayoutParams

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
                group.bind<ItemSiteIntervalBinding>()
                    .changeLayoutParams(matchWidth = true, matchHeight = false))
        }
    }

    override fun canSwipe(): DirectionInfo {
        return DirectionInfo.NONE
    }

    override fun canMove(): DirectionInfo {
        return DirectionInfo.VERTICAL
    }

}