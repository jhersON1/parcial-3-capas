package com.example.app.datos.compra

import android.content.ContentValues
import android.content.Context
import com.example.app.datos.base.Conexion

class DDetalleCompra(context: Context) {
    private val bd: Conexion = Conexion.instancia(context)

    private var cantidad: Int = 0
    private var costoUnitario: Double = 0.0
    private var idCompra: Int = 0
    private var idProducto: Int = 0

    fun setCantidad(v:Int) { cantidad = v }
    fun setCostoUnitario(v:Double) { costoUnitario = v }
    fun setIdCompra(v:Int) { idCompra = v }

    fun setProducto(v:Int) { idProducto = v }

    fun crear(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply {
            put("id_compra", idCompra)
            put("id_producto", idProducto)
            put("cantidad", cantidad)
            put("costo_unitario", costoUnitario)
        }
        return db.insert("detalle_compra", null, cv) != -1L
    }

    fun eliminarPorCompra(id:Int): Boolean {
        val db = bd.writableDatabase
        return db.delete("detalle_compra", "id_compra=?", arrayOf(id.toString())) > 0
    }

    fun listarPorCompra(id:Int): List<Map<String, Any?>> {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT id_producto, cantidad, costo_unitario FROM detalle_compra WHERE id_compra=?",
            arrayOf(id.toString())
        )
        val lista = mutableListOf<Map<String, Any?>>()
        cur.use {
            while (it.moveToNext()) {
                lista.add(
                    mapOf(
                        "id_producto" to it.getInt(0),
                        "cantidad" to it.getInt(1),
                        "costo_unitario" to it.getDouble(2)
                    )
                )
            }
        }
        return lista
    }
}

