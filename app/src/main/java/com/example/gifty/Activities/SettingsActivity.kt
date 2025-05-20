package com.example.gifty.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.gifty.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Обрабатываем нажатие кнопки "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Обработка клика по пункту "Мой аккаунт"
        findViewById<TextView>(R.id.myaccount).setOnClickListener {
            val intent = Intent(this, MyAccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // Обработка клика по пункту "Сообщить о проблеме"
        findViewById<TextView>(R.id.problems).setOnClickListener {
            // ссылка на Google Form для обратной связи
            val url = "https://docs.google.com/forms/d/e/1FAIpQLSdvzcco5bNdhMdAI6YSUuzmERDHoenCg2UW71SsUM4klbhhXg/viewform?usp=dialog"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent) // Открываем веб-страницу формы
            overridePendingTransition(0, 0)
        }

        // Обработка клика по пункту "Возможности Gifty"
        findViewById<TextView>(R.id.info).setOnClickListener {
            // Путь к файлу в приватной директории приложения
            val filePath = File(filesDir, "Руководство пользователя.pdf")
            // Проверяем наличие файла руководства пользователя на устройстве
            if (!filePath.exists()) {
                try {
                    // Копируем файл руководства из ресурсов Assets в файловую систему устройства
                    copyAssetToFile("Руководство пользователя.pdf", filePath)
                } catch (e: IOException) {
                    // Сообщаем об ошибке открытия файла
                    Toast.makeText(this, "Ошибка открытия файла: ${e.message}", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Генерируем защищённый URI для открытия файла PDF
            val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", filePath)

            // Формируем интент для открытия файла PDF
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "application/pdf")
            // Предоставляем разрешение на чтение файла
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Запускаем просмотрщик PDF
            startActivity(intent)
        }
    }


    @Throws(IOException::class)
    private fun copyAssetToFile(assetFileName: String, outFile: File) {
        // Открываем поток чтения ресурса из папки assets
        val inputStream: InputStream = assets.open(assetFileName)
        // Открываем выходной поток записи файла
        val outputStream = FileOutputStream(outFile)
        // Переносим данные из входящего потока в исходящий
        outputStream.use { output ->
            inputStream.copyTo(output)
        }
    }
}