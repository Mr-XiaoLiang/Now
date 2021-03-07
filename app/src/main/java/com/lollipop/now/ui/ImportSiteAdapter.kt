package com.lollipop.now.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.now.data.SiteInfo

/**
 * @author lollipop
 * @date 11/12/20 19:46
 */
class ImportSiteAdapter(
    private val data: ArrayList<SiteInfo>,
    private val isSelected: (SiteInfo) -> Boolean,
    private val onSiteClick: (Int) -> Unit): RecyclerView.Adapter<ImportSiteHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportSiteHolder {
        return ImportSiteHolder.create(parent) {
            onSiteClick(it.adapterPosition)
        }
    }

    override fun onBindViewHolder(holder: ImportSiteHolder, position: Int) {
        val siteInfo = data[position]
        holder.bind(siteInfo, isSelected(siteInfo))
    }

    override fun getItemCount(): Int {
        return data.size
    }

}