package com.example.app.datos.venta

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.app.datos.base.Conexion

class DVenta(context: Context) {
    private val bd: Conexion = Conexion.instancia(context)

    private var fechaHora: String = ""
    private var id: Int = 0
    private var total: Double = 0.0

    fun setFechaHora(v:String) { fechaHora = v }
    fun setId(v:Int) { id = v }
    fun setTotal(v:Double) { total = v }
    fun getId(): Int = id // Propuesta para el diagrama: exponer id generado

    // Método exacto del diagrama que devuelve int
    fun crear(): Int {
        val db = bd.writableDatabase
        val cv = ContentValues().apply { put("fechaHora", fechaHora); put("total", total) }
        val rowId = db.insert("venta", null, cv)
        if (rowId == -1L) return -1
        // Obtener id autogenerado
        val c: Cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        c.use { if (it.moveToFirst()) id = it.getLong(0).toInt() }
        return id
    }
    
    // Método de compatibilidad
    fun crearVenta(): Boolean {
        return crear() != -1
    }
}

