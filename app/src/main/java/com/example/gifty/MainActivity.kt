package com.example.gifty

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    //Объявляем переменную для нижней навигационной панели
    private lateinit var btnNavView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var  sp = getSharedPreferences("PC", Context.MODE_PRIVATE)
        sp.edit().putString("TY", "9").commit()

        // Инициализация нижней навигационной панели
        btnNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val controller = findNavController(R.id.fragmentContainerView)
        btnNavView.setupWithNavController(controller)
        btnNavView.setOnNavigationItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setEnterAnim(0)
                .setExitAnim(0)
                .setPopEnterAnim(0)
                .setPopExitAnim(0)
                .build()
            when (item.itemId) {
                R.id.calendarFragment -> {
                    controller.navigate(R.id.calendarFragment, null, navOptions)
                    true
                }
                R.id.searchFragment -> {
                    controller.navigate(R.id.searchFragment, null, navOptions)
                    true
                }
                R.id.homeFragment -> {
                    controller.navigate(R.id.homeFragment, null, navOptions)
                    true
                }
                else -> false
            }
        }
    }
}
