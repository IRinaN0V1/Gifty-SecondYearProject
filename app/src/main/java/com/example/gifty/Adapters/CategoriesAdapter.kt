package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gifty.Adapters.GiftsAdapter.OnGiftClickListener
import com.example.gifty.CategoryData
import com.example.gifty.Gift
import com.example.gifty.R


class CategoriesAdapter(
    private val items: List<CategoryData>,
    private val selectedCategoryIds: MutableList<Int>,
    private val selectedCategoryNames: MutableList<String>,
    private val context: Context,
    private val categoryType: String
) : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>()  {
    inner class CategoriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryCheckBox: CheckBox = view.findViewById(R.id.categoryCheckBox)
        val categoryName: TextView = view.findViewById(R.id.categoryNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categories_list_item, parent, false)
        return CategoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val item = items[position]
        holder.categoryName.text = item.name

        // Сброс слушателя перед обновлением состояния
        holder.categoryCheckBox.setOnCheckedChangeListener(null)

        // Определяем обработчик кликов
        holder.categoryCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Если поставили галочку — добавляем в списки
                if (!selectedCategoryIds.contains(item.id)) {
                    selectedCategoryIds.add(item.id)
                    selectedCategoryNames.add(item.name)
                }
            } else {
                // Если сняли галочку — удаляем из списков
                selectedCategoryIds.remove(item.id)
                selectedCategoryNames.remove(item.name)
            }
            // Сохраняем изменения
            saveSelectedCategories(context)
        }

        // Загружаем ранее выбранное значение
        holder.categoryCheckBox.isChecked = selectedCategoryIds.contains(item.id)
    }

    private fun saveSelectedCategories(context: Context) {
        val idKey = when(categoryType) {
            "Хобби" -> "selected_hobbies_ids"
            "Праздник" -> "selected_holidays_ids"
            "Профессия" -> "selected_professions_ids"
            else -> error("Invalid category type: $categoryType")
        }

        val nameKey = when(categoryType) {
            "Хобби" -> "selected_hobbies_names"
            "Праздник" -> "selected_holidays_names"
            "Профессия" -> "selected_professions_names"
            else -> error("Invalid category type: $categoryType")
        }

        val sharedPref = context.getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putStringSet(idKey, HashSet(selectedCategoryIds.map { it.toString() }))
        editor.putStringSet(nameKey, HashSet(selectedCategoryNames))
        editor.apply()
    }

    override fun getItemCount(): Int = items.size

}