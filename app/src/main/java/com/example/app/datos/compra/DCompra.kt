package com.example.app.datos.compra

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.app.datos.base.Conexion

class DCompra(context: Context) {
    private val bd: Conexion = Conexion.instancia(context)

    private var fechaHora: String = ""
    private var id: Int = 0

    fun setFechaHora(v:String) { fechaHora = v }
    fun setId(v:Int) { id = v }
    fun getId(): Int = id

    fun crear(): Int {
        val db = bd.writableDatabase
        val cv = ContentValues().apply { put("fechaHora", fechaHora) }
        val rowId = db.insert("compra", null, cv)
        if (rowId == -1L) return -1
        val c: Cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        c.use { if (it.moveToFirst()) id = it.getLong(0).toInt() }
        return id
    }

    fun crearCompra(): Boolean {
        return crear() != -1
    }
}

