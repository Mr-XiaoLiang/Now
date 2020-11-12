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
import com.lollipop.now.list.SwipeableHolder

/**
 * @author lollipop
 * @date 11/11/20 18:50
 */
class EmptyHolder
private constructor(
    view: View): RecyclerView.ViewHolder(view), MovableHolder, SwipeableHolder {

    companion object {
        fun create(group: ViewGroup): EmptyHolder {
            return EmptyHolder(
                LayoutInflater.from(group.context)
                    .inflate(R.layout.item_empty, group, false))
        }
    }

    override fun canMove(): DirectionInfo {
        return DirectionInfo.NONE
    }

    override fun canSwipe(): DirectionInfo {
        return DirectionInfo.NONE
    }
}