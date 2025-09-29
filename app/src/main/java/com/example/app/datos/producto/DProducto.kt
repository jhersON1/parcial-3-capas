package com.example.app.datos.producto

import android.content.ContentValues
import android.content.Context
import com.example.app.datos.base.Conexion

class DProducto(context: Context) {

    private val bd: Conexion = Conexion.instancia(context)

    private var id: Int = 0
    private var nombre: String = ""
    private var descripcion: String = ""
    private var precio: Double = 0.0
    private var stock: Int = 0
    private var categoriaId: Int = 0

    // Setters que usa N
    fun setId(v:Int) { id = v }
    fun setNombre(v:String) { nombre = v }
    fun setDescripcion(v:String) { descripcion = v }
    fun setPrecio(v:Double) { precio = v }
    fun setStock(v:Int) { stock = v }
    fun setCategoriaId(v:Int) { categoriaId = v }

    // Métodos de persistencia
    fun crear(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply {
            // Si id <= 0, omitimos el campo para que SQLite autogenere el ID
            if (id > 0) put("id", id)
            put("nombre", nombre); put("descripcion", descripcion)
            put("precio", precio); put("stock", stock); put("categoria_id", categoriaId)
        }
        return db.insert("producto", null, cv) != -1L
    }

    fun editar(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply {
            put("nombre", nombre); put("descripcion", descripcion)
            put("precio", precio); put("stock", stock); put("categoria_id", categoriaId)
        }
        return db.update("producto", cv, "id=?", arrayOf(id.toString())) > 0
    }

    fun eliminar(): Boolean {
        val db = bd.writableDatabase
        return db.delete("producto", "id=?", arrayOf(id.toString())) > 0
    }

    /**
     * Elimina el producto forzando: borra primero los movimientos en
     * detalle_venta y detalle_compra que referencian al producto.
     * Útil para demos donde no se requiere integridad histórica.
     */
    fun eliminarForzado(): Boolean {
        val db = bd.writableDatabase
        var ok = false
        db.beginTransaction()
        try {
            db.delete("detalle_venta", "id_producto=?", arrayOf(id.toString()))
            db.delete("detalle_compra", "id_producto=?", arrayOf(id.toString()))
            ok = db.delete("producto", "id=?", arrayOf(id.toString())) > 0
            if (ok) db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return ok
    }

    fun listar(filtro:String): List<Map<String, Any?>> {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT id, nombre, descripcion, precio, stock, categoria_id FROM producto WHERE nombre LIKE ? ORDER BY nombre",
            arrayOf("%$filtro%")
        )
        val lista = mutableListOf<Map<String, Any?>>()
        cur.use {
            while (it.moveToNext()) {
                lista.add(
                    mapOf(
                        "id" to it.getInt(0),
                        "nombre" to it.getString(1),
                        "descripcion" to it.getString(2),
                        "precio" to it.getDouble(3),
                        "stock" to it.getInt(4),
                        "categoria_id" to it.getInt(5)
                    )
                )
            }
        }
        return lista
    }

    // --- Consultas auxiliares requeridas por los diagramas ---
    fun existeMovimientos(prodId: Int): Boolean {
        val db = bd.readableDatabase
        val c1 = db.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM detalle_venta WHERE id_producto=?)",
            arrayOf(prodId.toString())
        )
        val c2 = db.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM detalle_compra WHERE id_producto=?)",
            arrayOf(prodId.toString())
        )
        c1.use { if (it.moveToFirst() && it.getInt(0) == 1) return true }
        c2.use { if (it.moveToFirst() && it.getInt(0) == 1) return true }
        return false
    }

    /** Ajusta el stock según delta (+ para compra, - para venta) */
    fun ajustaStock(delta: Int): Boolean {
        val db = bd.writableDatabase
        val cur = db.rawQuery("SELECT stock FROM producto WHERE id=?", arrayOf(id.toString()))
        val nuevo = cur.use { if (it.moveToFirst()) it.getInt(0) + delta else return false }
        if (nuevo < 0) return false
        val cv = ContentValues().apply { put("stock", nuevo) }
        return db.update("producto", cv, "id=?", arrayOf(id.toString())) > 0
    }

    // --- Alias 1:1 con el diagrama ---
    fun crea(): Boolean = crear()
    fun elimina(): Boolean = eliminar()
    fun lista(filtro: String): List<Map<String, Any?>> = listar(filtro)
}
