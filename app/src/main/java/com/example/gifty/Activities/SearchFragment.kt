package com.example.gifty.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.GiftsAdapter
import com.example.gifty.Api
import com.example.gifty.Data.Gift
import com.example.gifty.R
import com.example.gifty.ViewModels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), GiftsAdapter.OnGiftClickListener {
    @Inject
    lateinit var api: Api

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var giftsAdapter: GiftsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var noResultsMessage: TextView

    private var shouldUpdateGifts = true
    private var currentSearchText: String = ""
    private var lastSearchedText: String = ""
    private lateinit var randomGifts: List<Gift>
    private lateinit var originalGifts: List<Gift>
    private var lastFilteredGifts: List<Gift>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Инициализация элементов интерфейса
        recyclerView = view.findViewById(R.id.gifts_view)
        progressBar = view.findViewById(R.id.progressBar)
        noResultsMessage = view.findViewById(R.id.no_results_message)
        searchInput = view.findViewById(R.id.inputBtn)
        val paramSearchBtn: ConstraintLayout = view.findViewById(R.id.paramSearchBtn)
        val settingsBtn: ImageView = view.findViewById(R.id.settings)

        // Обработчик нажатия кнопоки настроек
        settingsBtn.setOnClickListener {
            shouldUpdateGifts = false
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        // Обработчик нажатия кнопки поиска по параметрам
        paramSearchBtn.setOnClickListener {
            shouldUpdateGifts = false
            val intent = Intent(requireContext(), ParametersSearchActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Наблюдение за изменениями в модели представления
        observeViewModel()

        // Установка обработчиков изменений текста
        setupListeners()

        return view
    }

    // Метод наблюдения за изменением состояний ViewModel
    private fun observeViewModel() {
        // Наблюдатель за изменением списка отображаемых подарков
        viewModel.allGifts.observe(viewLifecycleOwner) { allGifts ->
            if (shouldUpdateGifts && currentSearchText.isEmpty()) {
                // Если в строке поиска ничего нет — показываем случайные подарки
                randomGifts = getRandomGifts(allGifts, 30)
                originalGifts = randomGifts
                displayGifts(randomGifts)
            } else if (currentSearchText.isNotEmpty()) {
                // Если в строке поиска что-то введено, фильтруем список подарков
                viewModel.filterGifts(currentSearchText)
            }
        }

        // Наблюдатель за изменением отфильтрованного списка подарков
        viewModel.filteredGifts.observe(viewLifecycleOwner) { filteredGifts ->
            if (filteredGifts.isNullOrEmpty()) {
                // Нет результатов поиска — сообщаем пользователю
                noResultsMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                displayGifts(originalGifts)
            } else {
                // Есть результаты поиска — скрываем сообщение "нет результатов"
                noResultsMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val randomFilteredGifts = getRandomGifts(filteredGifts, 30)

                // Сохраняем последнее состояние подарков
                lastFilteredGifts = randomFilteredGifts

                // Показываем результат
                displayGifts(randomFilteredGifts)
            }
        }
    }

    // Функция выбора случайных подарков из общего списка
    private fun getRandomGifts(gifts: List<Gift>, count: Int): List<Gift> {
        return gifts.shuffled().take(count.coerceAtMost(gifts.size))
    }

    // Отображение списка подарков
    private fun displayGifts(gifts: List<Gift>) {
        giftsAdapter = GiftsAdapter(gifts, requireContext(), this@SearchFragment)
        recyclerView.adapter = giftsAdapter
        progressBar.visibility = View.GONE
    }

    // Установка слушателя изменений текста в поле поиска
    private fun setupListeners() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { text ->
                    currentSearchText = text.trim().toString()
                    // Если поиск пуст — возвращаемся к оригинальному списку
                    if (currentSearchText.isEmpty()) {
                        displayGifts(originalGifts)
                    } else {
                        // Выполняем поиск только при изменении самого текста
                        if (currentSearchText != lastSearchedText) {
                            viewModel.filterGifts(currentSearchText)
                            lastSearchedText = currentSearchText
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        // Загружаем общий список подарков только если поисковая строка пуста
        if (currentSearchText.isEmpty()) {
            viewModel.getGifts()
        } else {
            // Иначе используем последний результат поиска
            lastFilteredGifts?.let { displayGifts(it) } ?: run {
                viewModel.filterGifts(currentSearchText)
            }
        }
    }

    // Обработка кликов по подаркам
    override fun onGiftClick(gift: Gift) {
        shouldUpdateGifts = false
        val intent = Intent(requireContext(), GiftActivity::class.java)
        intent.putExtra("giftName", gift.name)
        intent.putExtra("giftDescription", gift.description)
        intent.putExtra("giftImage", gift.image)
        intent.putExtra("giftId", gift.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)
    }
}