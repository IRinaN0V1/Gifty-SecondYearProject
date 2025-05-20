package com.example.gifty.Adapters

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.BitmapFactory
import com.example.gifty.Data.Form
import com.example.gifty.R
import java.io.File
import android.widget.ImageView

class FormsAdapter(
    private val context: Context,
    private var forms: List<Form>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FormsAdapter.FormViewHolder>() {

    inner class FormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formName: TextView = itemView.findViewById(R.id.form_name) // Название анкеты
        val formImage: ImageView = itemView.findViewById(R.id.form_image) // Изображение анкеты

        init {
            // Добавляем обработчик длинного нажатия для вызова всплывающего меню
            itemView.setOnLongClickListener {
                showPopupMenu(itemView, adapterPosition)
                true
            }
        }

        // Всплывающее меню для анкеты
        private fun showPopupMenu(view: View, position: Int) {
            val popupMenu = PopupMenu(context, view)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.form_actions_menu, popupMenu.menu)

            // Добавляем обработчик выбора пункта меню
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        val form = forms[position]
                        // Вызываем обработчик редактирования
                        listener.onEditClicked(form)
                        true
                    }
                    R.id.action_delete -> {
                        val form = forms[position]
                        // Вызываем обработчик удаления
                        listener.onDeleteClicked(form)
                        true
                    }
                    R.id.action_make_report -> {
                        val form = forms[position]
                        // Вызываем обработчик формирования отчёта
                        listener.onReportClicked(form)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show() // Показываем меню
        }
    }

    // Создание ViewHolder для каждого элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forms_list_item, parent, false)
        return FormViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        val form = forms[position] // Текущая анкета
        holder.formName.text = form.name // Устанавливаем название анкеты

        // Загрузка изображения анкеты, если оно доступно
        if (form.image != "null") {
            val file = File(form.image)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.path)
                holder.formImage.setImageBitmap(bitmap)
            }
        }

        // Обработчик обычного клика по анкете
        holder.itemView.setOnClickListener {
            listener.onItemClicked(form)
        }
    }

    // Возвращает количество элементов в списке
    override fun getItemCount(): Int = forms.size

    // Интерфейс для внешних обработчиков событий
    interface OnItemClickListener {
        fun onItemClicked(form: Form) // Простой клик по элементу
        fun onEditClicked(form: Form) // Редактирование анкеты
        fun onDeleteClicked(form: Form) // Удаление анкеты
        fun onReportClicked(form: Form) // Формирование отчета по анкете
    }
}