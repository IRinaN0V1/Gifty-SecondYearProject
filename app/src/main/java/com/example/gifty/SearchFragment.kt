package com.example.gifty

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.CategoriesAdapter
import com.example.gifty.Adapters.GiftsAdapter
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), GiftsAdapter.OnGiftClickListener {
    @Inject
    lateinit var api: Api
    private lateinit var giftsAdapter: GiftsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var noResultsMessage: TextView
    private var allGifts: List<Gift> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view.findViewById(R.id.gifts_view)
        progressBar = view.findViewById(R.id.progressBar)
        noResultsMessage = view.findViewById(R.id.no_results_message)
        searchInput = view.findViewById(R.id.inputBtn)
        val paramSearchBtn: ConstraintLayout = view.findViewById(R.id.paramSearchBtn)
        val settingsBtn: ImageView = view.findViewById(R.id.settings)
        settingsBtn.setOnClickListener {
            // Создаем Intent, используя context текущей активности
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            requireActivity().startActivity(intent)
            // Убираем анимацию перехода
            requireActivity().overridePendingTransition(0, 0)
        }


        paramSearchBtn.setOnClickListener{
            val intent = Intent(requireContext(), ParametersSearchActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        // Инициализация RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2) // По два элемента в строке
        loadGifts() // Получаем список подарков

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Метод вызывается перед изменением текста
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Метод вызывается в момент изменения текста
                filterGifts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Метод вызывается после изменения текста
            }
        })

        return view
    }

    private fun loadGifts() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getGifts()
                val jsonResponse = response.body()
                if (jsonResponse != null && !jsonResponse.get("error").asBoolean) {
                    val giftsArray = jsonResponse.getAsJsonArray("gifts")
                    allGifts = jsonConverter(giftsArray)
                    withContext(Dispatchers.Main) {
                        giftsAdapter = GiftsAdapter(allGifts, requireContext(), this@SearchFragment)
                        recyclerView.adapter = giftsAdapter
                        progressBar.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(requireContext(), "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MyLog", "Exception occurred in ${javaClass.simpleName}: ${e.message}")
            }
        }
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


    private fun filterGifts(query: String) {
        val filteredGifts = allGifts.filter { gift ->
            gift.name.contains(query, ignoreCase = true) // Фильтруем по имени подарка
        }
        if (filteredGifts.isEmpty()) {
            noResultsMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noResultsMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            giftsAdapter = GiftsAdapter(filteredGifts, requireContext(), this) // Передаем контекст и слушателя
            recyclerView.adapter = giftsAdapter
        }
    }

    override fun onGiftClick(gift: Gift) {
        val intent = Intent(requireContext(), GiftActivity::class.java)
        intent.putExtra("giftName", gift.name)
        intent.putExtra("giftDescription", gift.description)
        intent.putExtra("giftImage", gift.image)
        intent.putExtra("giftId", gift.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)
    }
}