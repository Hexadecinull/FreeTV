package com.phstudio.freetv.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.phstudio.freetv.R

internal class ItemAdapter2(
    private val context: Context,
    private var itemList: ArrayList<Triple<String, String, String>>,
    private val listener: OnItemClickListener,
    private val longClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<ItemAdapter2.MyViewHolder>() {

    /** Exposed so LinkActivity can check the current query during addItems() */
    var currentQuery: String = ""

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int): Boolean
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btItem: Button = view.findViewById(R.id.btItem)

        init {
            btItem.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) listener.onItemClick(pos)
            }
            btItem.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) longClickListener.onItemLongClick(pos)
                true
            }
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) listener.onItemClick(pos)
            }
            itemView.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) longClickListener.onItemLongClick(pos)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button2, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (name, logoUrl, _) = itemList[position]
        holder.btItem.text = name
        holder.btItem.isFocusable = true
        holder.btItem.isClickable = true

        // Load logo with Coil — replaces Picasso boilerplate
        if (logoUrl.isNotBlank()) {
            holder.btItem.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            val iv = ImageView(context).apply { maxWidth = 80; maxHeight = 80 }
            iv.load(logoUrl) {
                allowHardware(false)
                listener(
                    onSuccess = { _, result ->
                        val d = result.drawable
                        holder.btItem.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
                    },
                    onError = { _, _ -> }
                )
            }
        } else {
            holder.btItem.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
