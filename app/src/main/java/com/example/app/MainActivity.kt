package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirigir directamente a la gestión de productos
        val intent = Intent(this, com.example.app.presentacion.producto.PProducto::class.java)
        startActivity(intent)
        finish()
    }
}
