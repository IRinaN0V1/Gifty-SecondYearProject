package com.example.gifty.Activities

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.ChoseFormAdapter
import com.example.gifty.Api
import com.example.gifty.Data.Form
import com.example.gifty.R
import com.example.gifty.ViewModels.ChoseFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@AndroidEntryPoint
class ChoseFormActivity : AppCompatActivity() {
    @Inject
    lateinit var api: Api

    private val viewModel: ChoseFormViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChoseFormAdapter
    private var giftId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chose_form)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        giftId = intent.getIntExtra("giftId", -1)

        // Обработка нажатия на кнопку "Назад"
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
        // Наблюдаем за изменениями списка форм
        viewModel.forms.observe(this) { forms ->
            // Отображаем формы, когда данные загружены
            displayForms(forms)
        }
    }

    override fun onResume() {
        super.onResume()
        // Загружаем формы при переходе на активность
        loadForms()
    }

    private fun loadForms() {
        val userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        // Загружаем формы по идентификатору пользователя
        viewModel.getForms(userId)
    }

    private fun displayForms(forms: List<Form>) {
        // Инициализируем адаптер и задаем его для RecyclerView
        adapter = ChoseFormAdapter(this, forms, object : ChoseFormAdapter.OnItemClickListener {
            override fun onItemClicked(form: Form) {
                // Создаем диалог для подтверждения добавления подарка в выбранную анкету
                AlertDialog.Builder(this@ChoseFormActivity)
                    .setTitle("Подтверждение")
                    .setMessage("Вы уверены, что хотите добавить подарок в анкету \"${form.name}\"?")
                    .setPositiveButton("Да") { dialog, _ ->
                        // Запускаем корутину для обработки запроса
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Запрашиваем информацию о подарке
                                val response = api.getSelectedGiftByGiftIdAndFormId(giftId, form.id)
                                val jsonResponse = response.body()
                                if (jsonResponse != null) {
                                    // Получаем сообщение из ответа
                                    val message = jsonResponse.get("message").asString
                                    // Проверяем, был ли подарок уже добавлен
                                    if (!jsonResponse.get("flag").asBoolean){
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try{
                                                // Добавляем подарок в анкету
                                                val responseAddGiftToForm = api.addGiftToForm(giftId, form.id)
                                            }catch (e: Exception) {
                                                Log.e("MyLog", "Exception occurred: ${e.message}")
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@ChoseFormActivity, message, Toast.LENGTH_LONG).show()
                                            dialog.dismiss()
                                        }
                                    } else{
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@ChoseFormActivity, message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MyLog", "Exception occurred: ${e.message}")

                            }
                        }
                    }
                    // Закрываем диалог, если пользователь нажал "Нет"
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
                    .show()
            }
        })
        // Установка адаптера для RecyclerView
        recyclerView.adapter = adapter
    }
}