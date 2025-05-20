package com.example.gifty.Activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.SelectedGiftsAdapter
import com.example.gifty.Data.Gift
import com.example.gifty.R
import com.example.gifty.ViewModels.FormViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FormActivity : AppCompatActivity(), SelectedGiftsAdapter.OnGiftClickListener {
    private val viewModel: FormViewModel by viewModels()
    private lateinit var giftsAdapter: SelectedGiftsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultsMessage: TextView
    private lateinit var deleteBtn: ImageView
    private var id: Int = -1
    private lateinit var image2: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        image2 = findViewById(R.id.avatar)
        // Получаем данные анкеты из интента
        id = intent.getIntExtra("formId", -1)
        val name = intent.getStringExtra("formName")
        val birthday = intent.getStringExtra("formBirthday")
        val image = intent.getStringExtra("formImage")

        val recipientName: TextView = findViewById(R.id.recipientName)
        val recipientBirthday: TextView = findViewById(R.id.birthday)

        val backBtn: ImageView = findViewById(R.id.backButton)
        deleteBtn = findViewById(R.id.deleteBtn)

        // Устанавливаем название анкеты и дату рождения получателя
        recipientName.text = formatName(name)
        recipientBirthday.text = birthday

        image?.let {
            // Загружаем изображение получателя по пути
            loadImage(it)
        }

        // Инициализация RecyclerView для отображения подарков
        recyclerView = findViewById(R.id.gifts_view)
        // Отображение подарков по 2 в строке
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        giftsAdapter = SelectedGiftsAdapter(emptyList(), this, this)
        recyclerView.adapter = giftsAdapter
        noResultsMessage = findViewById(R.id.no_results_message)

        // Наблюдение за изменениями списка подарков
        viewModel.gifts.observe(this) { gifts ->
            // Отфильтровываем подарки, которые уже выбраны
            val allGifts = gifts.filter { gift ->
                !giftsAdapter.getSelectedGiftIds().contains(gift.id.toString())
            }
            // Проверяем, есть ли в анкете подарки
            if (allGifts.isEmpty()) {
                // Показываем сообщение об отсутствии подарков
                noResultsMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                // Отображаем подарки
                giftsAdapter = SelectedGiftsAdapter(allGifts, this@FormActivity, this@FormActivity)
                recyclerView.adapter = giftsAdapter
            }
        }

        // Наблюдение за сообщениями из модели представления
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Загрузка подобранных под анкету подарков по идентификатору анкеты
        viewModel.loadSelectedGifts(id)

        // Обработка клика на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Обработка клика на кнопку удаления подарков
        deleteBtn.setOnClickListener {
            deleteSelectedGifts()
        }

        // Обработка клика на главное пространство для скрытия чекбоксов
        findViewById<View>(R.id.main).setOnClickListener {
            hideCheckboxes()
        }
    }

    // Метод для загрузки изображения анкеты
    private fun loadImage(filePath: String) {
        // Создание файла из переданного пути
        val file = File(filePath)
        if (file.exists()) {
            // Декодируем файл в Bitmap
            val bitmap = BitmapFactory.decodeFile(file.path)
            // Устанавливаем изображение в ImageView
            image2.setImageBitmap(bitmap)
        }
    }

    // Обработка клика на подарок
    override fun onGiftClick(gift: Gift) {
        // Создаем Intent для перехода к GiftActivity с информацией о подарке
        val intent = Intent(this, GiftActivity::class.java).apply {
            putExtra("giftName", gift.name)
            putExtra("giftDescription", gift.description)
            putExtra("giftImage", gift.image)
            putExtra("giftId", gift.id)
        }
        startActivity(intent)
        this.overridePendingTransition(0, 0)
    }

    // Метод для удаления выбранных подарков
    private fun deleteSelectedGifts() {
        // Получаем идентификаторы выбранных подарков
        val selectedGiftIds = giftsAdapter.getSelectedGiftIds()
        if (selectedGiftIds.isEmpty()) {
            // Если нет выбранных подарков, показываем сообщение
            showAlert("Уведомление", "Пожалуйста, выберите подарки для удаления")
        } else {
            // Показываем диалог подтверждения удаления
            showConfirmationDialog(selectedGiftIds.size) { confirm ->
                if (confirm) {
                    // Удаляем выбранные подарки
                    viewModel.deleteSelectedGifts(id, selectedGiftIds)
                    // Перезагружаем список подарков
                    viewModel.loadSelectedGifts(id)
                    // Скрываем кнопку удаления
                    hideDeleteBtn()
                }
            }
        }
    }

    // Метод для показа кнопки удаления
    fun showDeleteBtn() {
        deleteBtn.visibility = View.VISIBLE
    }

    // Метод для скрытия кнопки удаления
    fun hideDeleteBtn() {
        deleteBtn.visibility = View.GONE
    }

    // Метод для скрытия чекбоксов
    fun hideCheckboxes() {
        if (::giftsAdapter.isInitialized) {
            // Отключаем отображение чекбоксов
            giftsAdapter.showSelection = false
            // Очищаем списки выбранных подарков
            giftsAdapter.clearSelectedGiftIds()
            // Уведомляем адаптер об изменениях
            giftsAdapter.notifyDataSetChanged()
            // Скрываем кнопку удаления
            hideDeleteBtn()
        }
    }

    // Метод для форматирования имени
    private fun formatName(name: String?): String {
        val maxLength = 15 // Максимальная длина имени
        return if (!name.isNullOrEmpty() && name.length > maxLength) {
            // Обрезаем имя и добавляем точки в конце
            name.substring(0, maxLength) + "..."
        } else {
            // Возвращаем пустую строку, если имя null
            name ?: ""
        }
    }

    // Метод для отображения диалога подтверждения удаления
    private fun showConfirmationDialog(selectedCount: Int, onConfirm: (Boolean) -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить $selectedCount подарок(ов)?")
            .setPositiveButton("Да") { _, _ -> onConfirm(true) }
            .setNegativeButton("Нет") { _, _ -> onConfirm(false) }
            .create()
        alertDialog.show()
    }

    // Метод для отображения предупреждения
    private fun showAlert(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .create()
        alertDialog.show()
    }
}
