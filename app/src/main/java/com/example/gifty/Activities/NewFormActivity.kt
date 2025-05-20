package com.example.gifty.Activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.gifty.R
import com.example.gifty.ViewModels.NewFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import com.yalantis.ucrop.UCrop

@AndroidEntryPoint
class NewFormActivity : AppCompatActivity() {
    private val viewModel: NewFormViewModel by viewModels()
    // Константа для уникального кода выбора фото
    private val IMAGE_PICK_CODE = 1000
    private lateinit var image: ImageView
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_form)

        // Получаем данные, переданные через Intent
        val formName = intent.getStringExtra("formName")
        val formBirthday = intent.getStringExtra("formBirthday")
        val formId = intent.getIntExtra("formId", -1)
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)
        val formImage = intent.getStringExtra("formImage")

        val recipientName: EditText = findViewById(R.id.recipientName)
        val birthdayText: EditText = findViewById(R.id.birthday)
        val title: TextView = findViewById(R.id.textView)
        val buttonText: TextView = findViewById(R.id.newFormButtonTextView)
        val newFormButton: ConstraintLayout = findViewById(R.id.newFormButton)
        val calendarBtn: ImageView = findViewById(R.id.calendarButton)
        val backButton: ImageView = findViewById(R.id.backButton)
        image = findViewById(R.id.addPhotoButton)

        // Добавляем обработчик для выбора фотографии
        image.setOnClickListener {
            // Проверяем разрешения доступа к медиа
            if (arePermissionsGranted()) {
                openImagePicker()
            } else {
                requestPermissions()
            }
        }

        // Если имя анкеты и дата рождения переданы, значит это редактирование
        if (formName != null && formBirthday != null) {
            title.text = "Изменение анкеты"
            recipientName.setText(formName)
            birthdayText.setText(formBirthday)
            buttonText.text = "Сохранить"
            if (formImage != "null") {
                // Проверяем существование файла изображения
                val file = File(formImage)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.path)
                    image.setImageBitmap(bitmap)
                }
            }
        } else {
            // Иначе устанавливаем заголовок для создания анкеты
            title.text = "Новая анкета"
            buttonText.text = "Создать"
        }

        // Выбор даты рождения
        calendarBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, y, m, d ->
                    // Записываем выбранную дату в поле ввода в формате дд.мм.гггг
                    birthdayText.setText(String.format("%02d.%02d.%d", d, m + 1, y))
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Обработчик кнопки создания или изменения анкеты
        newFormButton.setOnClickListener {
            val name = recipientName.text.toString()
            val birthday = birthdayText.text.toString()
            if (name.isNotBlank() && birthday.isNotBlank()) {
                if (formId > 0) {
                    // Обновляем существующую анкету
                    viewModel.updateForm(formId, name, birthday, imagePath ?: "null")
                } else {
                    // Создаем новую анкету
                    viewModel.checkIfFormExists(userId, name)
                    viewModel.checkIfFormExistsResult.observe(this) { result ->
                        if (result) {
                            Toast.makeText(this, "Анкета с таким именем уже существует", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createForm(name, imagePath ?: "null", birthday, userId)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        // Наблюдаем за результатом создания новой анкеты
        viewModel.createResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Анкета создана", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Возникла ошибка. Попробуйте ещё раз.", Toast.LENGTH_SHORT).show()
            }
        }
        // Наблюдаем за результатом изменения анкеты
        viewModel.changeFormResult.observe(this) { result ->
            if (result) {
                Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Не удалось сохранить изменения", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработчик нажатия кнопки назад
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    // Открытие галереи для выбора изображения
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Обрабатываем результат выбора изображения
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            uri?.let {
                // Получаем реальный путь к файлу изображения
                val filePath = getRealPathFromURI(it)
                Log.d("MyLog", "Selected image URI: $it")
                Log.d("MyLog", "File path: $filePath")

                // Отображаем выбранное изображение
                image.setImageURI(it)
                imagePath = getRealPathFromURI(it)
            }
        }
    }

    // Получаем реальный путь к файлу по URI
    private fun getRealPathFromURI(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            // Получаем путь к файлу изображения
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(uri, projection, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(columnIndex ?: -1)
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    // Обработка результатов запросов разрешений
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                    permissions[Manifest.permission.READ_MEDIA_VIDEO] == true) {
                }
            } else {
                if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Запрашиваем необходимые разрешения
    private fun requestPermissions() {
        val permissionsToRequest = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
            else -> {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        requestPermissionLauncher.launch(permissionsToRequest)
    }

    // Проверяем, предоставлены ли требуемые разрешения
    private fun arePermissionsGranted(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this, Manifest.permission.READ_MEDIA_VIDEO
                        ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}