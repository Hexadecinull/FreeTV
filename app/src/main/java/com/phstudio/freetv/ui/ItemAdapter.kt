package com.phstudio.freetv.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.phstudio.freetv.R

internal class ItemAdapter(
    private var itemList: ArrayList<Triple<String, Int, String>>,
    private val listener: OnItemClickListener,
    private val longClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<ItemAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int): Boolean
    }

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (names, icons, _) = Triple(
            itemList.map { it.first },
            itemList.map { it.second },
            itemList.map { it.third }
        )
        holder.btItem.text = names[position]
        holder.btItem.isFocusable = true
        holder.btItem.isClickable = true
        holder.btItem.setCompoundDrawablesWithIntrinsicBounds(icons[position], 0, 0, 0)
    }

    override fun getItemCount(): Int = itemList.size
}
