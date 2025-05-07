package com.example.gifty


import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class NewFormActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_form)
        // Получаем переданные данные из Intent
        val formName = intent.getStringExtra("formName")
        var formBirthday: String? = intent.getStringExtra("formBirthday")
        val formId = intent.getIntExtra("formId", -1)
        var  sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
        var newFormButton: ConstraintLayout = findViewById(R.id.newFormButton)
        var recipientName: EditText = findViewById(R.id.recipientName)
        var birthdayText: EditText = findViewById(R.id.birthday)
        var title: TextView = findViewById(R.id.textView)
        var buttonText: TextView = findViewById(R.id.newFormButtonTextView)
        val calendarBtn = findViewById<ImageView>(R.id.calendarButton)

        if (formBirthday != null){
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            // Парсим входную дату
            val date = inputFormat.parse(formBirthday)
            // Форматируем дату в нужный формат
            val formattedDate = outputFormat.format(date)
            birthdayText.setText(formattedDate)
        }

        val calendar = Calendar.getInstance()
        calendarBtn.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    formBirthday  = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    birthdayText.setText(String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear))
                },
                year, month, dayOfMonth
            )
            datePickerDialog.show()
        }

        if (formName != null && formBirthday != null) {
            title.text = "Изменение анкеты"
            recipientName.setText(formName)
            buttonText.text = "Сохранить"
            newFormButton.setOnClickListener{
                // Проверяем корректность заполнения полей
                if (recipientName.text.isEmpty() || birthdayText.text.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                // Запуск корутины для обновления формы
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.updateForm(formId, recipientName.text.toString(), formBirthday!!)
                        val jsonResponse = response.body()
                        if (jsonResponse != null) {
                            val message = jsonResponse.get("message").asString
                            if (!jsonResponse.get("error").asBoolean){
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@NewFormActivity, message, Toast.LENGTH_LONG).show()
                                    finish() // Закрытие активности
                                    overridePendingTransition(0, 0)
                                }
                            } else{
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@NewFormActivity, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MyLog", "Exxception occurred in ${javaClass.simpleName}: ${e.message}")
                    }
                }
            }
        } else{
            newFormButton.setOnClickListener{
                if (recipientName.text.isEmpty() || recipientName.text.toString() == " "){
                    Toast.makeText(this, "Укажите имя", Toast.LENGTH_LONG).show()
                }
                else if (formBirthday!!.isEmpty()){
                    Toast.makeText(this, "Укажите дату рождения", Toast.LENGTH_LONG).show()
                }
                else{

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = api.getFormByUserIdAndName(recipientName.text.toString(), sp.getInt("UserId", -1))
                            val jsonResponse = response.body()
                            if (jsonResponse != null) {
                                val message = jsonResponse.get("message").asString
                                if (!jsonResponse.get("flag").asBoolean){
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val createFormResponse = api.createForm(recipientName.text.toString(),"null" ,formBirthday!!, sp.getInt("UserId", -1))
                                    }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@NewFormActivity, message, Toast.LENGTH_LONG).show()
                                    }
                                    finish()
                                    overridePendingTransition(0, 0)
                                } else{
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@NewFormActivity, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
                        }
                    }
                }

            }
        }

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }
}

