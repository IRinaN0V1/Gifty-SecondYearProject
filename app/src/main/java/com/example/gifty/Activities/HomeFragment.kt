package com.example.gifty.Activities

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.FormsAdapter
import com.example.gifty.Api
import com.example.gifty.Data.Form
import com.example.gifty.R
import com.example.gifty.ViewModels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var api: Api
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FormsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем наличие разрешений на показ уведомлений
        if (!areNotificationsEnabled(requireContext())) {
            // Если разрешение отсутствует, выводим диалоговое окно с просьбой разрешить уведомления
            showRequestNotificationPermissionDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val settingsBtn: ImageView = view.findViewById(R.id.settings)
        // Обработка нажатия на кнопку настроек
        settingsBtn.setOnClickListener {
            // Переходим на экран настроек
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
            requireActivity().overridePendingTransition(0, 0)
        }

        val newFormButton: ConstraintLayout = view.findViewById(R.id.addNewForm)
        // Переход на экран создания новой анкеты при нажатии на кнопку
        newFormButton.setOnClickListener {
            startActivity(Intent(requireContext(), NewFormActivity::class.java))
            requireActivity().overridePendingTransition(0, 0)
        }

        // Наблюдаем за изменениями в модели представления
        observeViewModel()
        // Загружаем список анкет пользователя
        loadForms()
    }

    // Наблюдаем за изменениями в модели представления
    private fun observeViewModel() {
        // Изменение списка анкет
        viewModel.forms.observe(viewLifecycleOwner) { forms ->
            // Метод отображения анкет
            displayForms(forms)
        }

        // Результат удаления анкеты
        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), "Анкета удалена", Toast.LENGTH_LONG).show()
                loadForms()
            } else {
                Toast.makeText(requireContext(), "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Загрузка списка анкет пользователя
    private fun loadForms() {
        // Получаем идентификатор пользователя
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        // Получаем анкеты
        viewModel.getForms(userId)
    }

    override fun onResume() {
        super.onResume()
        // Загрузка списка анкет при переходе на фрагмент
        loadForms()
    }

    private fun displayForms(forms: List<Form>) {
        // Создаем новый адаптер с переданными анкетами
        adapter = FormsAdapter(requireContext(), forms, object : FormsAdapter.OnItemClickListener {
            override fun onItemClicked(form: Form) {
                // Открываем анкету при клике на неё
                val intent = Intent(requireContext(), FormActivity::class.java)
                intent.putExtra("formName", form.name)
                intent.putExtra("formBirthday", form.birthday)
                intent.putExtra("formId", form.id)
                intent.putExtra("formImage", form.image)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }

            override fun onEditClicked(form: Form) {
                // Открываем страницу редактирования анкеты
                val intent = Intent(requireContext(), NewFormActivity::class.java)
                intent.putExtra("formName", form.name)
                intent.putExtra("formBirthday", form.birthday)
                intent.putExtra("formId", form.id)
                intent.putExtra("formImage", form.image)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }

            override fun onDeleteClicked(form: Form) {
                // Показываем диалог подтверждения удаления формы
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Подтверждение удаления")
                    setMessage("Вы уверены, что хотите удалить анкету ${form.name}?")
                    setPositiveButton("Да") { _, _ ->
                        viewModel.deleteForm(form.id)
                    }
                    setNegativeButton("Нет") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create().show()
                }
            }

            override fun onReportClicked(form: Form) {
                // Переходим на экран настройки отчетов
                val intent = Intent(requireContext(), ReportSettingsActivity::class.java)
                intent.putExtra("formId", form.id)
                startActivity(intent)
            }
        })
        // Устанавливаем адаптер в RecyclerView
        recyclerView.adapter = adapter
    }

    // Диалог для запроса разрешения на отправку уведомлений
    private fun showRequestNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Разрешить уведомления?")
            .setPositiveButton("Разрешить") { _, _ ->
                // Запрашиваем разрешение на уведомления
                requestNotificationPermission(requireContext())
            }
            .setNegativeButton("Отменить", null)
            .show()
    }

    // Проверка наличия разрешения на отправку уведомлений
    private fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }

    // Запрос разрешения на использование уведомлений у пользователя
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}