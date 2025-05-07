package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gifty.Adapters.FormsAdapter.FormViewHolder
import com.example.gifty.Adapters.FormsAdapter.OnItemClickListener
import com.example.gifty.Form
import com.example.gifty.Gift
import com.example.gifty.R

class GiftsAdapter(private val gifts: List<Gift>,
                   private val context: Context,
                   private val listener: OnGiftClickListener,
    ) : RecyclerView.Adapter<GiftsAdapter.GiftViewHolder>() {

    inner class GiftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val giftImage: ImageView = view.findViewById(R.id.giftImage)
        val giftName: TextView = view.findViewById(R.id.giftName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gifts_list_item, parent, false)
        return GiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val gift = gifts[position]
        holder.giftName.text = gift.name
        Glide.with(holder.itemView.context).load(gift.image).into(holder.giftImage)

        holder.itemView.setOnClickListener {
            listener.onGiftClick(gift)
        }
    }


    override fun getItemCount(): Int = gifts.size

    interface OnGiftClickListener {
        fun onGiftClick(gift: Gift)
    }
}

