package com.example.app.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.app.R
import com.example.app.presentacion.categoria.PCategoria
import com.example.app.presentacion.producto.PProducto
import com.example.app.presentacion.venta.PVenta
import com.example.app.presentacion.compra.PCompra
import com.example.app.presentacion.reporte.PReporte

/**
 * Clase base que maneja la navegación del bottom navigation bar
 * para todas las actividades principales
 */
open class BaseActivity : AppCompatActivity() {
    
    protected fun setupBottomNavigation(bottomNavigation: BottomNavigationView, currentItem: Int) {
        // Marcar el item actual como seleccionado
        bottomNavigation.selectedItemId = currentItem
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_categorias -> {
                    if (currentItem != R.id.nav_categorias) {
                        startActivity(Intent(this, PCategoria::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_productos -> {
                    if (currentItem != R.id.nav_productos) {
                        startActivity(Intent(this, PProducto::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_ventas -> {
                    if (currentItem != R.id.nav_ventas) {
                        startActivity(Intent(this, PVenta::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_compras -> {
                    if (currentItem != R.id.nav_compras) {
                        startActivity(Intent(this, PCompra::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_reportes -> {
                    if (currentItem != R.id.nav_reportes) {
                        startActivity(Intent(this, PReporte::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
