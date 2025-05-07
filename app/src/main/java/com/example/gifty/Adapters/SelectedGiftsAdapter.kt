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
import com.example.gifty.FormActivity
import com.example.gifty.Gift
import com.example.gifty.R

class SelectedGiftsAdapter(private val gifts: List<Gift>,
                   private val context: Context,
                   private val listener: OnGiftClickListener,
) : RecyclerView.Adapter<SelectedGiftsAdapter.SelectedGiftsViewHolder>() {
    var showSelection = false
    private val selectedGiftIds = mutableListOf<String>()
    inner class SelectedGiftsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val giftImage: ImageView = view.findViewById(R.id.giftImage)
        val giftName: TextView = view.findViewById(R.id.giftName)
        val selectionOverlay: View = view.findViewById(R.id.selectionOverlay)
        val checkbox: ImageView = view.findViewById(R.id.checkbox)

        init {
            checkbox.setOnClickListener {
                val giftId = gifts[adapterPosition].id.toString()
                if (selectedGiftIds.contains(giftId)) {
                    selectedGiftIds.remove(giftId)
                    checkbox.setImageResource(R.drawable.checkbox)
                } else {
                    selectedGiftIds.add(giftId)
                    checkbox.setImageResource(R.drawable.checked_icon)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedGiftsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selectedgifts_list_item, parent, false)
        return SelectedGiftsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedGiftsViewHolder, position: Int) {
        val gift = gifts[position]
        holder.giftName.text = gift.name
        Glide.with(holder.itemView.context).load(gift.image).into(holder.giftImage)

        holder.selectionOverlay.visibility = if (showSelection) View.VISIBLE else View.GONE
        holder.checkbox.visibility = if (showSelection) View.VISIBLE else View.GONE

        // Устанавливаем состояние чекбокса
        if (selectedGiftIds.contains(gift.id.toString())) {
            holder.checkbox.setImageResource(R.drawable.checked_icon) // Показываем галочку
        } else {
            holder.checkbox.setImageResource(R.drawable.checkbox) // Убираем галочку
        }

        holder.itemView.setOnClickListener {
            if (!showSelection) {
                listener.onGiftClick(gift) // Переход на страницу подарка только если не в режиме выбора
            }
        }

        holder.itemView.setOnLongClickListener {
            showSelection = true
            (context as? FormActivity)?.showDeleteBtn()
            notifyDataSetChanged()
            true
        }
    }



    fun getSelectedGiftIds(): List<String> {
        return selectedGiftIds.toList()
    }

    override fun getItemCount(): Int = gifts.size

    interface OnGiftClickListener {
        fun onGiftClick(gift: Gift)
    }
}