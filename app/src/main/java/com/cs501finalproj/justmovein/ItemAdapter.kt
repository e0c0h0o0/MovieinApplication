package com.cs501finalproj.justmovein

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ItemAdapter(private var itemList: MutableList<Item>, private val layoutType: String) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image)
        val priceView: TextView = view.findViewById(R.id.item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (layoutType) {
            "listing_items" -> R.layout.listing_item
            else -> R.layout.item_grid_layout
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        Glide.with(holder.imageView.context)
            .load(item.imageUrl)
            .into(holder.imageView)
        holder.priceView.text = String.format("$%.2f", item.price ?: 0.00) // Handle possible null price

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ItemDetailActivity::class.java)
            intent.putExtra("ITEM_ID", item.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = itemList.size

    fun updateItemList(newItems: List<Item>) {
        // Log.d("ItemAdapter", "Updating item list with ${newItems.size} items.")
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
}