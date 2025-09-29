package com.example.app.datos.venta

import android.content.ContentValues
import android.content.Context
import com.example.app.datos.base.Conexion

class DDetalleVenta(context: Context) {
    private val bd: Conexion = Conexion.instancia(context)

    private var cantidad: Int = 0
    private var id_producto: Int = 0
    private var id_venta: Int = 0
    private var precioUnitario: Double = 0.0

    fun setCantidad(v:Int) { cantidad = v }
    fun setIdProducto(v:Int) { id_producto = v }
    fun setIdVenta(v:Int) { id_venta = v }
    fun setPrecioUnitario(v:Double) { precioUnitario = v }
    
    // Métodos de compatibilidad
    fun setProducto(v:Int) { id_producto = v }
    fun setVenta(v:Int) { id_venta = v }

    fun crear(): Boolean {
        val db = bd.writableDatabase
        val cv = ContentValues().apply {
            put("id_venta", id_venta)
            put("id_producto", id_producto)
            put("cantidad", cantidad)
            put("precio_unitario", precioUnitario)
        }
        return db.insert("detalle_venta", null, cv) != -1L
    }

    fun eliminarPorVenta(vId:Int): Boolean {
        val db = bd.writableDatabase
        return db.delete("detalle_venta", "id_venta=?", arrayOf(vId.toString())) > 0
    }

    fun listarPorVenta(vId:Int): List<Map<String, Any?>> {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT id_producto, cantidad, precio_unitario FROM detalle_venta WHERE id_venta=?",
            arrayOf(vId.toString())
        )
        val lista = mutableListOf<Map<String, Any?>>()
        cur.use {
            while (it.moveToNext()) {
                lista.add(
                    mapOf(
                        "id_producto" to it.getInt(0),
                        "cantidad" to it.getInt(1),
                        "precio_unitario" to it.getDouble(2)
                    )
                )
            }
        }
        return lista
    }
}

