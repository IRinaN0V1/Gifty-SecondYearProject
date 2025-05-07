package com.example.gifty
import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.FormsAdapter
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

const val REQUEST_CODE_REPORT_SETTINGS = 1001
@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var api: Api
    private companion object {
        const val REQUEST_CODE_REPORT_SETTINGS = 1002 // Выберите любое уникальное число
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FormsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!areNotificationsEnabled(requireContext())) {
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
        settingsBtn.setOnClickListener {
            // Создаем Intent, используя context текущей активности
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            requireActivity().startActivity(intent)
            // Убираем анимацию перехода
            requireActivity().overridePendingTransition(0, 0)
        }
        // Инициализация кнопки добавления новой анкеты
        val newFormButton: ConstraintLayout = view.findViewById(R.id.addNewForm)
        newFormButton.setOnClickListener {
            // Создаем Intent, используя context текущей активности
            val intent = Intent(requireContext(), NewFormActivity::class.java)
            requireActivity().startActivity(intent)
            // Убираем анимацию перехода
            requireActivity().overridePendingTransition(0, 0)
        }
    }
    private fun loadForms() {
        val sp = requireContext().getSharedPreferences("PC", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getFormsByUserId(sp.getInt("UserId", -1))
                val jsonResponse = response.body()
                Log.d("MyLog", "${jsonResponse}")
                if (jsonResponse != null) {
                    val formsArray = jsonResponse.getAsJsonArray("forms")
                    val formsList = jsonConverter(formsArray)
                    withContext(Dispatchers.Main) {
                        displayForms(formsList)
                    }
                    
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Возникла ошибка. Пожалуйста, попробуйте еще раз.", Toast.LENGTH_LONG).show()
                    }
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

    override fun onResume() {
        super.onResume()
        loadForms()  // Загружаем формы при каждом переходе на фрагмент
    }

    private fun parseDate(form: Form): String? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date: Date = dateFormat.parse(form.birthday) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = date

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1 // Месяцы начинаются с 0
            val year = calendar.get(Calendar.YEAR)

            String.format("%02d.%02d.%d", day, month, year)
        } catch (e: Exception) {
            // Логирование ошибки
            Log.e("MyLog", "Ошибка разбора даты: ${form.birthday}", e)
            null // В случае ошибки вернуть null
        }
    }


    private fun displayForms(forms: List<Form>) {
        adapter = FormsAdapter(requireContext(), forms, object : FormsAdapter.OnItemClickListener {
            override fun onItemClicked(form: Form) {
                val formattedBirthday = parseDate(form)
                val intent = Intent(requireContext(), FormActivity::class.java)
                intent.putExtra("formName", form.name)
                intent.putExtra("formBirthday", formattedBirthday) // Передаем отформатированную дату
                intent.putExtra("formId", form.id)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)

            }

            override fun onEditClicked(form: Form) {
                val intent = Intent(requireContext(), NewFormActivity::class.java)
                intent.putExtra("formName", form.name)
                intent.putExtra("formBirthday", form.birthday)
                intent.putExtra("formId", form.id)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }

            override fun onDeleteClicked(form: Form) {
                val sp = requireContext().getSharedPreferences("PC", Context.MODE_PRIVATE)
                // Создание AlertDialog для подтверждения удаления
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Подтверждение удаления")
                    setMessage("Вы уверены, что хотите удалить анкету ${form.name}?")

                    setPositiveButton("Да") { _, _ ->
                        // Логика удаления формы
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = api.deleteForm(form.id)
                                val jsonResponse = response.body()

                                if (jsonResponse != null) {
                                    val message = jsonResponse.get("message").asString
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                                    }
                                    loadForms()
                                }

                            } catch (e: Exception) {
                                Log.e("MyLog", e.message ?: "Unknown exception")
                            }
                        }
                    }

                    setNegativeButton("Нет") { dialog, _ ->
                        dialog.dismiss() // Закрыть диалог при выборе "Нет"
                    }

                    create().show() // Показать диалог
                }
            }

            override fun onReportClicked(form: Form) {
                val intent = Intent(requireContext(), ReportSettingsActivity::class.java)
                intent.putExtra("formId", form.id)
                startActivityForResult(intent,0)

            }
        })
        recyclerView.adapter = adapter
    }

    private fun showRequestNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Разрешить уведомления?")
            .setPositiveButton("Разрешить") { _, _ ->
                requestNotificationPermission(requireContext())
            }
            .setNegativeButton("Отменить", null)
            .show()
    }
    // Функция для проверки разрешения на уведомления
    private fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }

    // Запрос разрешения на уведомления
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}