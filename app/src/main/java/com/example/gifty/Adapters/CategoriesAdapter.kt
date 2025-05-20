package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Data.CategoryData
import com.example.gifty.R

class CategoriesAdapter(
    private val items: List<CategoryData>,
    private val selectedCategoryIds: MutableList<Int>,
    private val selectedCategoryNames: MutableList<String>,
    private val context: Context,
    private val categoryType: String
) : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    // Внутренний класс для хранения View-элементов каждого пункта списка
    inner class CategoriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryCheckBox: CheckBox = view.findViewById(R.id.categoryCheckBox) // Чекбокс для выбора категории
        val categoryName: TextView = view.findViewById(R.id.categoryNameTextView) // Название категории
    }

    // Создание ViewHolder для отображения одного элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        // создаем экземпляр ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categories_list_item, parent, false)
        return CategoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val item = items[position]  // текущий элемент списка
        holder.categoryName.text = item.name // название категории

        // Сбрасываем предыдущий обработчик изменения состояния чекбокса
        holder.categoryCheckBox.setOnCheckedChangeListener(null)

        // Устанавливаем новый обработчик смены состояния чекбокса
        holder.categoryCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Если выбрана категория — добавляем её идентификатор и название в соответствующие списки
                if (!selectedCategoryIds.contains(item.id)) {
                    selectedCategoryIds.add(item.id)
                    selectedCategoryNames.add(item.name)
                }
            } else {
                // Если пользователь убрал выделение — удаляем категорию из списков
                selectedCategoryIds.remove(item.id)
                selectedCategoryNames.remove(item.name)
            }
            // Сохраняем выбранные категории
            saveSelectedCategories(context)
        }

        // Восстанавливаем предыдущее состояние чекбокса
        holder.categoryCheckBox.isChecked = selectedCategoryIds.contains(item.id)
    }

    // Метод сохранения выбранных категорий
    private fun saveSelectedCategories(context: Context) {
        // Определяем ключи для хранения ID и имен категорий в зависимости от типа
        val idKey = when (categoryType) {
            "hobbies" -> "selected_hobbies_ids"
            "holidays" -> "selected_holidays_ids"
            "professions" -> "selected_professions_ids"
            else -> error("Invalid category type: $categoryType")
        }

        val nameKey = when (categoryType) {
            "hobbies" -> "selected_hobbies_names"
            "holidays" -> "selected_holidays_names"
            "professions" -> "selected_professions_names"
            else -> error("Invalid category type: $categoryType")
        }

        // Получаем доступ к общим настройкам
        val sharedPref = context.getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Преобразовываем списки в множества и сохраняем в SharedPrefs
        editor.putStringSet(idKey, HashSet(selectedCategoryIds.map { it.toString() }))
        editor.putStringSet(nameKey, HashSet(selectedCategoryNames))
        editor.apply() // Применяем изменения
    }

    // Возврат количества элементов в списке
    override fun getItemCount(): Int = items.size
}