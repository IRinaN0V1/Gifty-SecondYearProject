package com.example.gifty.ViewModels

import android.app.Application
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifty.Data.Gift
import com.example.gifty.Data.Form
import com.example.gifty.Interactors.FormInteractor
import com.example.gifty.Interactors.GiftInteractor
import com.example.gifty.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ReportSettingsViewModel @Inject constructor(
    private val giftInteractor: GiftInteractor,
    private val formInteractor: FormInteractor,
    private val application: Application
) : ViewModel() {

    private val _form = MutableLiveData<Form?>()
    val form: LiveData<Form?> get() = _form

    private val _gifts = MutableLiveData<List<Gift>>()
    val gifts: LiveData<List<Gift>> get() = _gifts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun loadFormAndGifts(formId: Int) {
        loadForm(formId)
        loadGifts(formId)
    }

    private fun loadForm(formId: Int) {
        viewModelScope.launch {
            try {
                val formResponse = formInteractor.getFormById(formId)
                if (formResponse != null) {
                    _form.value = formResponse
                } else {
                    _message.value = "Ошибка загрузки формы."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Ошибка загрузки формы."
            }
        }
    }

    private fun loadGifts(formId: Int) {
        viewModelScope.launch {
            try {
                val giftsResponse = giftInteractor.getSelectedGifts(formId)
                _gifts.value = giftsResponse
            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Ошибка загрузки подарков."
            }
        }
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint): Float {
        val pageWidth = 792 // Задаем ширину страницы
        val words = text.split(" ") // Разделяем текст на слова
        var line = "" // Переменная для текущей строки
        var verticalOffset = y // Начальное вертикальное смещение

        // Проходим по каждому слову
        for (word in words) {
            val testLine = "$line $word" // Формируем строку с добавленным словом
            // Проверяем, не превышает ли ширина строки допустимую ширину страницы
            if (paint.measureText(testLine) > pageWidth - 100) {
                // Если превышает, рисуем текущую строку
                canvas.drawText(line.trim(), x, verticalOffset, paint)
                line = word // Начинаем новую строку с текущего слова
                verticalOffset += 20F // Увеличиваем вертикальное смещение для следующей строки
            } else {
                line = testLine // В противном случае обновляем текущую строку
            }
        }
        // Рисуем последнюю строку, если она не пуста
        if (line.isNotEmpty()) {
            canvas.drawText(line.trim(), x, verticalOffset, paint)
            verticalOffset += 20F // Увеличиваем смещение после последней строки
        }

        return verticalOffset // Возвращаем новое вертикальное смещение
    }

    fun generateAndSavePDF(saveUri: Uri, includeName: Boolean, includeBirthDate: Boolean, includeGiftName: Boolean, includeDescription: Boolean) {
        viewModelScope.launch {
            val pageHeight = 1120
            val pageWidth = 792
            val pdfDocument = PdfDocument()
            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20F
                color = ContextCompat.getColor(application, R.color.black)
            }
            val textPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 15F
                color = ContextCompat.getColor(application, R.color.black)
            }

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawText("Список идей для подарка", 100F, 100F, titlePaint)
            var verticalOffset = 150F

            val currentForm = _form.value
            if (currentForm != null) {
                if (includeName) {
                    canvas.drawText("Имя: ${currentForm.name}", 100F, verticalOffset, textPaint)
                    verticalOffset += 20F
                }
                if (includeBirthDate) {
                    canvas.drawText("Дата рождения: ${currentForm.birthday}", 100F, verticalOffset, textPaint)
                    verticalOffset += 20F
                }
            }
            canvas.drawText("Подарки:", 100F, verticalOffset, textPaint)
            verticalOffset += 20F

            val currentGifts = _gifts.value ?: emptyList()
            if (currentGifts.isEmpty()) {
                canvas.drawText("Нет доступных подарков.", 100F, verticalOffset, textPaint)
            } else {
                var giftIndex = 1
                for (gift in currentGifts) {
                    if (includeGiftName) {
                        canvas.drawText("$giftIndex. ${gift.name}", 100F, verticalOffset, textPaint)
                        verticalOffset += 20F // Existing line height
                    }

                    verticalOffset += 15F

                    if (includeDescription) {
                        verticalOffset = drawMultilineText(canvas, gift.description, 100F, verticalOffset, textPaint) // Align left with a margin
                    }
                    giftIndex++
                }
            }

            pdfDocument.finishPage(page)

            try {
                application.contentResolver.openOutputStream(saveUri)?.use { outStream ->
                    pdfDocument.writeTo(outStream)
                }
                Toast.makeText(application, "PDF файл добавлен в загрузки", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(application, "Не удалось сгенерировать PDF файл.", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        }
    }
}