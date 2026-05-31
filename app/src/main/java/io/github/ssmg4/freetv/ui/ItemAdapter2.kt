package io.github.ssmg4.freetv.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import io.github.ssmg4.freetv.R

internal class ItemAdapter2(
    private var itemList: ArrayList<Triple<String, String, String>>,
    private val listener: OnItemClickListener,
    private val longClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<ItemAdapter2.MyViewHolder>() {

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
        val (rawName, logoUrl, _) = itemList[position]

        // Strip iptv-org status badges from channel names:
        // Ⓖ = geoblocked, Ⓓ = Dailymotion, Ⓨ = YouTube, etc.
        val name = rawName.replace(Regex("[ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ①②③④⑤⑥⑦⑧⑨⑩]+\\s*"), "").trim()

        holder.btItem.text = name
        holder.btItem.isFocusable = true
        holder.btItem.isClickable = true

        if (logoUrl.isNotBlank()) {
            val context = holder.itemView.context
            val iconSizePx = (48 * context.resources.displayMetrics.density).toInt()
            val request = ImageRequest.Builder(context)
                .data(logoUrl)
                .size(iconSizePx, iconSizePx)
                .target(
                    onSuccess = { drawable ->
                        drawable.setBounds(0, 0, iconSizePx, iconSizePx)
                        holder.btItem.setCompoundDrawablesRelative(drawable, null, null, null)
                    },
                    onError = {
                        holder.btItem.setCompoundDrawablesRelative(null, null, null, null)
                    }
                )
                .build()
            context.imageLoader.enqueue(request)
        } else {
            holder.btItem.setCompoundDrawablesRelative(null, null, null, null)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
