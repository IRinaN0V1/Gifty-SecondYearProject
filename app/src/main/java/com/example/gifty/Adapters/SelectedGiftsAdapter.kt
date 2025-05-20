package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gifty.Activities.FormActivity
import com.example.gifty.Data.Gift
import com.example.gifty.R

class SelectedGiftsAdapter(
    private val gifts: List<Gift>,
    private val context: Context,
    private val listener: OnGiftClickListener
) : RecyclerView.Adapter<SelectedGiftsAdapter.SelectedGiftsViewHolder>() {
    // Видны ли чекбоксы
    var showSelection = false

    // Список идентификаторов выбранных подарков
    private val selectedGiftIds = mutableListOf<String>()

    inner class SelectedGiftsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val giftImage: ImageView = view.findViewById(R.id.giftImage)
        val giftName: TextView = view.findViewById(R.id.giftName)
        val selectionOverlay: View = view.findViewById(R.id.selectionOverlay)
        val checkbox: ImageView = view.findViewById(R.id.checkbox)

        init {
            // Обработчик нажатия на чекбокс
            checkbox.setOnClickListener {
                val giftId = gifts[adapterPosition].id.toString() // ID текущего подарка
                if (selectedGiftIds.contains(giftId)) {
                    // Если подарок уже выбран - удаляем его из списка выбранных подарков
                    selectedGiftIds.remove(giftId)
                    checkbox.setImageResource(R.drawable.checkbox)
                } else {
                    // Если подарок ещё не выбран, добавляем его в список
                    selectedGiftIds.add(giftId)
                    checkbox.setImageResource(R.drawable.checked_icon)
                }
            }
        }
    }

    // Создание ViewHolder для каждого элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedGiftsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selectedgifts_list_item, parent, false)
        return SelectedGiftsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedGiftsViewHolder, position: Int) {
        val gift = gifts[position] // Текущий подарок

        // Устанавливаем название подарка
        holder.giftName.text = gift.name

        // Загружаем изображение подарка
        Glide.with(holder.itemView.context).load(gift.image).into(holder.giftImage)

        // Управление отображением накладки и чекбокса в зависимости от того, выбран подарок или нет
        holder.selectionOverlay.visibility = if (showSelection) View.VISIBLE else View.GONE
        holder.checkbox.visibility = if (showSelection) View.VISIBLE else View.GONE

        // Устанавливаем состояние чекбокса
        if (selectedGiftIds.contains(gift.id.toString())) {
            holder.checkbox.setImageResource(R.drawable.checked_icon)
        } else {
            holder.checkbox.setImageResource(R.drawable.checkbox)
        }

        // Обработчик клика по элементу
        holder.itemView.setOnClickListener {
            // Если подарки не в режиме выбора, переходим на страницу подарка
            if (!showSelection) {
                listener.onGiftClick(gift)
            }
        }

        // Обработчик долгого нажатия (включает режим выбора)
        holder.itemView.setOnLongClickListener {
            showSelection = true
            (context as? FormActivity)?.showDeleteBtn() // Показываем кнопку удаления
            notifyDataSetChanged() // Обновляем весь список подарков
            true
        }
    }

    // Получение списка выбранных подарков
    fun getSelectedGiftIds(): List<String> {
        return selectedGiftIds.toList()
    }

    // Очистка списка выбранных подарков
    fun clearSelectedGiftIds() {
        selectedGiftIds.clear()
    }

    // Возвращает количество элементов в списке
    override fun getItemCount(): Int = gifts.size

    // Интерфейс для обработчика кликов
    interface OnGiftClickListener {
        fun onGiftClick(gift: Gift)
    }
}