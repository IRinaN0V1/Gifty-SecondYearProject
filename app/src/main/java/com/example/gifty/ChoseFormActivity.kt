package com.example.gifty

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.ChoseFormAdapter
import com.example.gifty.Adapters.FormsAdapter
import com.example.gifty.Adapters.GiftsAdapter
import com.google.gson.JsonArray
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
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        loadForms()
    }

    private fun loadForms() {
        val sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getFormsByUserId(sp.getInt("UserId", -1))
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val formsArray = jsonResponse.getAsJsonArray("forms")
                    val formsList = jsonConverter(formsArray)
                    withContext(Dispatchers.Main) {
                        displayForms(formsList)
                    }
                } else {
                    Toast.makeText(this@ChoseFormActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", e.message ?: "Unknown exception")
            }
        }
    }

    private fun jsonConverter(jsonArray: JsonArray): List<Form> {
        val list = mutableListOf<Form>()
        jsonArray.forEach { jsonElement ->
            val jsonObject = jsonElement.asJsonObject
            val listElement = Form(
                id = jsonObject.get("id").asInt,
                name = jsonObject.get("name").asString,
                birthday = jsonObject.get("birthday").asString
            )
            list.add(listElement)
        }
        return list
    }

    private fun displayForms(forms: List<Form>) {
        adapter = ChoseFormAdapter(this, forms, object : ChoseFormAdapter.OnItemClickListener {
            override fun onItemClicked(form: Form) {
                AlertDialog.Builder(this@ChoseFormActivity)
                    .setTitle("Подтверждение")
                    .setMessage("Вы уверены, что хотите добавить подарок в анкету \"${form.name}\"?")
                    .setPositiveButton("Да") { dialog, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = api.getSelectedGiftByGiftIdAndFormId(giftId, form.id)
                                val jsonResponse = response.body()
                                if (jsonResponse != null) {
                                    val message = jsonResponse.get("message").asString
                                    if (!jsonResponse.get("flag").asBoolean){
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try{
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
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
                    .show()
            }
        })
        recyclerView.adapter = adapter
    }
}