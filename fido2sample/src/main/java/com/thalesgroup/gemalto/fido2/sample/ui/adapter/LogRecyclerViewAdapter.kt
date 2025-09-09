/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thalesgroup.gemalto.fido2.sample.R

class LogRecyclerViewAdapter : RecyclerView.Adapter<LogRecyclerViewAdapter.LogRVHolder?>() {
    private var items: MutableList<String?>? = null

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogRVHolder {
        return LogRVHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LogRVHolder, position: Int) {
        val item = items?.get(position)
        holder.textView.setText(item)
        holder.textView.setOnClickListener(View.OnClickListener { v: View? ->
            if (this@LogRecyclerViewAdapter.onItemClickListener != null) {
                this@LogRecyclerViewAdapter.onItemClickListener?.onClickCopy(items?.get(holder.getAdapterPosition()))
            }
        })
    }

    override fun getItemCount(): Int {
        val currentItems = items
        if (currentItems == null) {
            return 0
        }
        return currentItems.size
    }

    fun setItems(items: MutableList<String?>?) {
        this.items = items
    }

    class LogRVHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById<TextView>(R.id.textView)
        }
    }

    interface OnItemClickListener {
        fun onClickCopy(item: String?)
    }
}
