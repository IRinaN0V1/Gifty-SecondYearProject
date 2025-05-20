package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.R

class SelectedCategoriesAdapter(
    private val selectedItems: MutableList<String>,
    private val context: Context
) : RecyclerView.Adapter<SelectedCategoriesAdapter.SelectedCategoryViewHolder>() {

    inner class SelectedCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.text) // название категории
    }

    // Создание ViewHolder для каждого элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_categories_item, parent, false)
        return SelectedCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedCategoryViewHolder, position: Int) {
        val item = selectedItems[position] // Текущая категория
        holder.categoryName.text = item // Устанавливаем название категории
    }

    // Возвращает количество элементов в списке
    override fun getItemCount(): Int = selectedItems.size
}