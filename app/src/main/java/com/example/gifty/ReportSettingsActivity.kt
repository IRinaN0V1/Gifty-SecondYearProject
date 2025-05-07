package com.example.gifty
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.gifty.Adapters.GiftsAdapter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

const val REQUEST_CODE_PICK_DIRECTORY: Int = 200
@AndroidEntryPoint
class ReportSettingsActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api
    private lateinit var gifts: List<Gift>
    private  lateinit var form: Form
    // Переменные для кнопок
    private lateinit var generatePDFbtn: ConstraintLayout
    private lateinit var checkboxIncludeName: CheckBox
    private lateinit var checkboxIncludeBirthDate: CheckBox
    private lateinit var checkboxGiftName: CheckBox
    private lateinit var checkboxDescription: CheckBox
    private lateinit var checkboxCost: CheckBox
    // Ширина и высота страницы PDF-файла
    private val pageHeight = 1120
    private val pageWidth = 792
    private var formId = -1

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_settings)

        // Инициализация переменных
        generatePDFbtn = findViewById(R.id.idBtnGeneratePDF)
        checkboxIncludeName = findViewById(R.id.checkbox_include_name)
        checkboxIncludeBirthDate = findViewById(R.id.checkbox_include_birth_date)
        checkboxGiftName = findViewById(R.id.checkbox_giftname)
        checkboxDescription = findViewById(R.id.checkbox_description)

        formId = intent.getIntExtra("formId", -1)

        Log.d("MyLog", "${formId}")
        CoroutineScope(Dispatchers.IO).launch {
            val response = api.getFormById(formId)
            val jsonResponse = response.body()
            if (jsonResponse != null) {
                val message = jsonResponse.get("message").asString
                if (!jsonResponse.get("error").asBoolean){
                    val temp = jsonResponse.getAsJsonObject("form")
                    form = convertJsonToForm(temp)
                } else{
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReportSettingsActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        generatePDFbtn.setOnClickListener {
            loadFormAndGifts(formId)
            pickSaveLocation()
        }

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }

    private fun convertJsonToForm(jsonObject: JsonObject): Form {
        val id = jsonObject.get("id").asInt
        val name = jsonObject.get("name").asString
        val birthday = jsonObject.get("birthday").asString
        return Form(id, name, birthday)
    }

    private fun pickSaveLocation() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "GFG.pdf")
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // ОБЯЗАТЕЛЬНЫЙ ВЫЗОВ БАЗОВОЙ РЕАЛИЗАЦИИ

        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val selectedUri = data?.data
            if (selectedUri != null) {
                generateAndSavePDF(selectedUri)
            }
        }
    }

    // Генерация и сохранение PDF-файла
    private fun generateAndSavePDF(saveUri: Uri) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) // Жирный шрифт для заголовка
            textSize = 20F // Размер шрифта заголовка
            color = ContextCompat.getColor(this@ReportSettingsActivity, R.color.black)
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) // Обычный шрифт для остального текста
            textSize = 15F // Размер шрифта для остального текста
            color = ContextCompat.getColor(this@ReportSettingsActivity, R.color.black)
        }

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Заголовок документа
        canvas.drawText("Список идей для подарка", 100F, 100F, titlePaint)

        // Добавляем информацию о получателе
        var verticalOffset = 150F // начальная высота для следующего текста

        if (checkboxIncludeName.isChecked) {
            canvas.drawText("Имя: ${form.name}", 100F, verticalOffset, textPaint)
            verticalOffset += 20F
        }
        if (checkboxIncludeBirthDate.isChecked) {
            canvas.drawText("Дата рождения: ${form.birthday}", 100F, verticalOffset, textPaint)
            verticalOffset += 20F
        }

        // Заголовок для подарков
        canvas.drawText("Подарки:", 100F, verticalOffset, textPaint)
        verticalOffset += 20F // отступ после заголовка


        if (gifts.isEmpty()) {
            canvas.drawText("Нет доступных подарков.", 100F, verticalOffset, textPaint)
        } else {
            var giftIndex = 1
            for (gift in gifts) {
                if (checkboxGiftName.isChecked) {
                    canvas.drawText("$giftIndex. ${gift.name}", 100F, verticalOffset, textPaint)
                    verticalOffset += 20F
                }
                if (checkboxDescription.isChecked) {
                    drawMultilineText(canvas, gift.description, 120F, verticalOffset, textPaint)
                    verticalOffset += 40F
                }
                giftIndex++
            }
        }

        pdfDocument.finishPage(page)

        try {
            contentResolver.openOutputStream(saveUri)?.use { outStream ->
                pdfDocument.writeTo(outStream)
            }
            Toast.makeText(this, "PDF файл успешно сгенерирован.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Не удалось сгенерировать PDF файл.", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }
    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val words = text.split(" ") // Разделяем текст на слова
        var line = ""
        var verticalOffset = y

        for (word in words) {
            val testLine = "$line $word"
            if (paint.measureText(testLine) > pageWidth - 200) { // Проверка ширины строки
                canvas.drawText(line, x, verticalOffset, paint) // Отрисовка текущей строки
                line = word // Начинаем новую строку с текущего слова
                verticalOffset += 20F // Увеличиваем вертикальный отступ для новой строки
            } else {
                line = testLine // Добавляем слово в текущую строку
            }
        }
        canvas.drawText(line.trim(), x, verticalOffset, paint) // Отрисовка последней строки
    }
    private fun jsonConverter(jsonArray: JsonArray): List<Gift> {
        val list = mutableListOf<Gift>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = Gift(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                image = jsonObject.get("image").asString,
                description = jsonObject.get("description").asString,
                gender = jsonObject.get("gender").asInt
            )
            list.add(listElement)
        }
        return list
    }


    private fun loadFormAndGifts(formId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val giftsResponse = api.getSelectedGifts(form.id)
                val jsonResponse = giftsResponse.body()
                if (jsonResponse != null) {
                    val giftsArray = jsonResponse.getAsJsonArray("selectedgifts")
                    gifts = jsonConverter(giftsArray)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReportSettingsActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportSettingsActivity, "Ошибка загрузки данных.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}