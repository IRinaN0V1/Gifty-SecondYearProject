package com.example.gifty.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Data.Form
import com.example.gifty.R
import java.io.File

class ChoseFormAdapter(
    private val context: Context,
    private var forms: List<Form>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ChoseFormAdapter.ChoseFormViewHolder>() {

    // Класс ViewHolder для каждого элемента списка
    inner class ChoseFormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formName: TextView = itemView.findViewById(R.id.form_name) // Название анкеты
        val formImage: ImageView = itemView.findViewById(R.id.form_image) // Изображение анкеты
    }

    // Создание ViewHolder для отображения элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoseFormViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forms_list_item, parent, false)
        return ChoseFormViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoseFormViewHolder, position: Int) {
        val form = forms[position]  // Текущая анкета
        holder.formName.text = form.name  // Устанавливаем название анкеты

        // Если указано изображение
        if (form.image != "null") {
            val file = File(form.image)  // Загружаем файл изображения
            // Если файл существует
            if (file.exists()) {
                // Декодируем файл изображения и устанавливаем его в ImageView
                val bitmap = BitmapFactory.decodeFile(file.path)
                holder.formImage.setImageBitmap(bitmap)
            }
        }

        // Обработчик клика по элементу списка
        holder.itemView.setOnClickListener {
            listener.onItemClicked(form)
        }
    }

    // Количество элементов в списке
    override fun getItemCount(): Int = forms.size

    // Интерфейс для передачи внешнего обработчика кликов
    interface OnItemClickListener {
        fun onItemClicked(form: Form)  // Метод, вызываемый при выборе элемента
    }
}