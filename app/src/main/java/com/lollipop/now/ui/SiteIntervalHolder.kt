package com.lollipop.now.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.R
import com.lollipop.now.list.DirectionInfo
import com.lollipop.now.list.MovableHolder
import com.lollipop.now.list.SwipeableHolder

/**
 * @author lollipop
 * @date 11/12/20 11:14
 */
class SiteIntervalHolder private constructor(view: View): RecyclerView.ViewHolder(view),
    SwipeableHolder, MovableHolder {

    companion object {
        fun create(group: ViewGroup): SiteIntervalHolder {
            return SiteIntervalHolder(
                LayoutInflater.from(group.context)
                    .inflate(R.layout.item_site_interval, group, false))
        }
    }

    override fun canSwipe(): DirectionInfo {
        return DirectionInfo.NONE
    }

    override fun canMove(): DirectionInfo {
        return DirectionInfo.NONE
    }

}