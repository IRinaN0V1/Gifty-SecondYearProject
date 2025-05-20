package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gifty.Data.Gift
import com.example.gifty.R

class GiftsAdapter(
    private val gifts: List<Gift>,
    private val context: Context,
    private val listener: OnGiftClickListener
) : RecyclerView.Adapter<GiftsAdapter.GiftViewHolder>() {

    inner class GiftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val giftImage: ImageView = view.findViewById(R.id.giftImage) // Изображение подарка
        val giftName: TextView = view.findViewById(R.id.giftName)    // Название подарка
    }

    // Создание ViewHolder для каждого элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gifts_list_item, parent, false)
        return GiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val gift = gifts[position] // Текущий подарок

        // Если изображение отсутствует, ставим картинку по умолчанию
        if (gift.image == "") {
            holder.giftImage.setImageResource(R.drawable._044950_no_image_icon__1_)
        }

        // Устанавливаем название подарка
        holder.giftName.text = gift.name

        // Загружаем изображение подарка
        Glide.with(holder.itemView.context).load(gift.image).into(holder.giftImage)

        // Обработчик клика по подарку
        holder.itemView.setOnClickListener {
            listener.onGiftClick(gift)
        }
    }

    // Возвращает количество элементов в списке
    override fun getItemCount(): Int = gifts.size

    // Интерфейс для внешнего обработчика кликов
    interface OnGiftClickListener {
        fun onGiftClick(gift: Gift)
    }
}