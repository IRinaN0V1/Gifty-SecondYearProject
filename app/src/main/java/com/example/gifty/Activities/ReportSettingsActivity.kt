 package com.example.gifty.Activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.example.gifty.R
import com.example.gifty.ViewModels.ReportSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

// Константа для кода запроса выбора директории
const val REQUEST_CODE_PICK_DIRECTORY: Int = 200

@AndroidEntryPoint
class ReportSettingsActivity : AppCompatActivity() {
    private lateinit var generatePDFbtn: ConstraintLayout
    private lateinit var checkboxIncludeName: CheckBox
    private lateinit var checkboxIncludeBirthDate: CheckBox
    private lateinit var checkboxGiftName: CheckBox
    private lateinit var checkboxDescription: CheckBox
    private var formId = -1 // Идентификатор анкеты

    private val viewModel: ReportSettingsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_settings)

        // Инициализация кнопок и чекбоксов из макета
        generatePDFbtn = findViewById(R.id.idBtnGeneratePDF)
        checkboxIncludeName = findViewById(R.id.checkbox_include_name)
        checkboxIncludeBirthDate = findViewById(R.id.checkbox_include_birth_date)
        checkboxGiftName = findViewById(R.id.checkbox_giftname)
        checkboxDescription = findViewById(R.id.checkbox_description)

        // Получаем идентификатор анкеты
        formId = intent.getIntExtra("formId", -1)

        // Загружаем данные анкеты и подобранные под нее подарки
        viewModel.loadFormAndGifts(formId)

        // Наблюдатель для сообщений от модели представления
        viewModel.message.observe(this, Observer { message ->
            message?.let {
                // Показываем сообщение
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })

        // Обработка нажатия на кнопку генерации PDF
        generatePDFbtn.setOnClickListener {
            // Выбор места для сохранения PDF
            pickSaveLocation()
        }

        // Обработка нажатия кнопки "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    // Метод для выбора места сохранения PDF-документа
    private fun pickSaveLocation() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Указываем, что документ открываемый
            addCategory(Intent.CATEGORY_OPENABLE)
            // Указываем тип файла
            type = "application/pdf"
            // Устанавливаем название файла
            putExtra(Intent.EXTRA_TITLE, "Gifty.pdf")
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY)
    }

    // Обработка результата выбора директории
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Проверяем, что это результат выбора директории и что результат успешный
        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            // Получаем URI выбранной директории
            val selectedUri = data?.data
            if (selectedUri != null) {
                // Генерируем и сохраняем PDF с выбранными параметрами
                viewModel.generateAndSavePDF(selectedUri,
                    checkboxIncludeName.isChecked,
                    checkboxIncludeBirthDate.isChecked,
                    checkboxGiftName.isChecked,
                    checkboxDescription.isChecked)
            }
        }
    }
}