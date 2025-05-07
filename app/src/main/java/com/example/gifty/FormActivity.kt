package com.example.gifty

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.GiftsAdapter
import com.example.gifty.Adapters.SelectedGiftsAdapter
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class FormActivity : AppCompatActivity(), SelectedGiftsAdapter.OnGiftClickListener {
    @Inject
    lateinit var api: Api
    private lateinit var giftsAdapter: SelectedGiftsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsMessage: TextView
    private lateinit var deleteBtn: ImageView
    private var id: Int = -1;
    private var allGifts: List<Gift> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form)

        id = intent.getIntExtra("formId", -1)
        val name = intent.getStringExtra("formName")
        val birthday = intent.getStringExtra("formBirthday")

        val recipientName: TextView = findViewById(R.id.recipientName)
        val recipientBirthday: TextView = findViewById(R.id.birthday)

        val backBtn: ImageView = findViewById(R.id.backButton)
        deleteBtn = findViewById(R.id.deleteBtn)

        findViewById<View>(R.id.main).setOnClickListener {
            // Скрыть чекбоксы, когда нажимаем на пустое место
            hideCheckboxes()
        }


        deleteBtn.setOnClickListener {
            // Логика для удаления выбранных подарков
            deleteSelectedGifts()
        }

        // Обработка нажатия на кнопку "Назад"
        backBtn.setOnClickListener {
            finish()  // Закрытие текущей активности и возврат к MainActivity
            // Убираем анимацию перехода
            overridePendingTransition(0, 0)
        }

        recipientName.text = name
        recipientBirthday.text = birthday

        recyclerView = findViewById(R.id.gifts_view)
//        progressBar = findViewById(R.id.progressBar)
        noResultsMessage = findViewById(R.id.no_results_message)

        // Инициализация RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2) // По два элемента в строке
        giftsAdapter = SelectedGiftsAdapter(emptyList(), this, this)
        recyclerView.adapter = giftsAdapter // Устанавливаем адаптер, чтобы избежать ошибок
        // Теперь дальше загружаем данные
        loadSelectedGifts()
        loadSelectedGifts() // Получаем список подарков
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

    private fun loadSelectedGifts() {
//        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getSelectedGifts(id)
                val jsonResponse = response.body()
                if (jsonResponse != null) {
                    if (!jsonResponse.get("error").asBoolean){
                        val giftsArray = jsonResponse.getAsJsonArray("selectedgifts")
                        allGifts = jsonConverter(giftsArray)
                        withContext(Dispatchers.Main) {
                            if (allGifts.isEmpty()) {
                                noResultsMessage.visibility = View.VISIBLE
                            } else {
                                giftsAdapter = SelectedGiftsAdapter(allGifts, this@FormActivity, this@FormActivity)
                                recyclerView.adapter = giftsAdapter
                            }
                        }
                    }
                    else{
                        withContext(Dispatchers.Main) {
                            noResultsMessage.visibility = View.VISIBLE
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormActivity, "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка: ${e.message}")
            }
        }
    }
    override fun onGiftClick(gift: Gift) {
        val intent = Intent(this, GiftActivity::class.java)
        intent.putExtra("giftName", gift.name)
        intent.putExtra("giftDescription", gift.description)
        intent.putExtra("giftImage", gift.image)
        intent.putExtra("giftId", gift.id)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
    }

    fun showDeleteBtn() {
        deleteBtn.visibility = View.VISIBLE
    }

    fun hideDeleteBtn() {
        deleteBtn.visibility = View.GONE
    }

    fun hideCheckboxes() {
        if (::giftsAdapter.isInitialized) {
            giftsAdapter.showSelection = false
            giftsAdapter.notifyDataSetChanged() // Уведомить адаптер о изменениях, чтобы скрыть чекбоксы
            hideDeleteBtn()
        }
    }

    private fun deleteSelectedGifts() {
        val selectedGiftIds = giftsAdapter.getSelectedGiftIds() // Получаем выбранные ID
        val selectedCount = selectedGiftIds.size

        if (selectedCount == 0) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Уведомление")
                .setMessage("Пожалуйста, выберите подарки для удаления")
                .setPositiveButton("ОК") { _, _ ->
                    performDelete(selectedGiftIds)
                }
            alertDialog.show()
        }
        else{
            // Запрос подтверждения у пользователя
            showConfirmationDialog(selectedCount)
        }
    }
    private fun showConfirmationDialog(selectedCount: Int) {
        val selectedGiftIds = giftsAdapter.getSelectedGiftIds()
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить $selectedCount подарок(ов)?")
            .setPositiveButton("Да") { _, _ ->
                performDelete(selectedGiftIds)
            }
            .setNegativeButton("Нет", null)
            .create()

        alertDialog.show()
    }

    private fun performDelete(selectedGiftIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val giftIds = selectedGiftIds.joinToString(",") // Преобразуем список ID в строку
                Log.d("MyLog", "${id},${giftIds}")
                val response = api.deleteSelectedGifts(id, giftIds)
                Log.d("MyLog", "${response}")
                val jsonResponse = response.body()
                if (jsonResponse != null) {
                    if (!jsonResponse.get("error").asBoolean){
                        allGifts = allGifts.filter { gift ->
                            !selectedGiftIds.contains(gift.id.toString())
                        }
                        withContext(Dispatchers.Main) {
                            if (allGifts.isEmpty()) {
                                noResultsMessage.visibility = View.VISIBLE // Показываем сообщение, если список пустой
                            } else {
                                noResultsMessage.visibility = View.GONE // Скрываем сообщение, если есть подарки
                            }
                            // Обновляем адаптер
                            giftsAdapter = SelectedGiftsAdapter(allGifts, this@FormActivity, this@FormActivity)
                            recyclerView.adapter = giftsAdapter // Устанавливаем новый адаптер
                            hideDeleteBtn() // Скрываем кнопку удаления
                        }
                    } else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FormActivity, "Не удалось удалить подарки", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("MyLog", "Ошибка: ${e.message}")
            }
        }
    }
}