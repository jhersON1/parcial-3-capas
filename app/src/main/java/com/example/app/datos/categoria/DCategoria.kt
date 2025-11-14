package com.example.app.datos.categoria

import android.content.ContentValues
import android.content.Context
import com.example.app.datos.base.Conexion

class DCategoria(context: Context) {

    private val bd: Conexion = Conexion.instancia(context)

    private var id: Int = 0
    private var nombre: String = ""

    fun setId(v:Int) { id = v }
    fun setNombre(v:String) { nombre = v }

    fun crear(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply { 
            if (id > 0) put("id", id) // Solo poner ID si es mayor a 0
            put("nombre", nombre) 
        }
        return db.insert("categoria", null, cv) != -1L
    }

    fun editar(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply { put("nombre", nombre) }
        return db.update("categoria", cv, "id=?", arrayOf(id.toString())) > 0
    }

    fun eliminar(): Boolean {
        val db = bd.writableDatabase
        return db.delete("categoria", "id=?", arrayOf(id.toString())) > 0
    }

    fun listar(filtro:String): List<Map<String, Any?>> {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT id, nombre FROM categoria WHERE nombre LIKE ? ORDER BY nombre",
            arrayOf("%$filtro%")
        )
        val lista = mutableListOf<Map<String, Any?>>()
        cur.use {
            while (it.moveToNext()) {
                lista.add(mapOf("id" to it.getInt(0), "nombre" to it.getString(1)))
            }
        }
        return lista
    }

    fun existeProductosEnCategoria(catId:Int): Boolean {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT COUNT(*) FROM producto WHERE categoria_id = ?",
            arrayOf(catId.toString())
        )
        cur.use { return if (it.moveToFirst()) it.getInt(0) > 0 else false }
    }

    fun existeCategoria(catId:Int): Boolean {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM categoria WHERE id=?)",
            arrayOf(catId.toString())
        )
        cur.use { return it.moveToFirst() && it.getInt(0) == 1 }
    }

    fun lista(filtro: String): List<Map<String, Any?>> = listar(filtro)
}
