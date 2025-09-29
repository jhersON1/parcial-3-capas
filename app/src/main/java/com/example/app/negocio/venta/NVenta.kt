package com.example.app.negocio.venta

import android.content.Context
import com.example.app.datos.producto.DProducto
import com.example.app.datos.venta.DDetalleVenta
import com.example.app.datos.venta.DVenta

class NVenta(private val context: Context) {

    fun validarItem(idProducto:Int, cantidad:Int, precioUnitario:Double): Boolean {
        if (idProducto <= 0 || cantidad <= 0 || precioUnitario < 0) return false
        val dp = DProducto(context)
        dp.setId(idProducto)

        return true
    }

    fun calcularTotal(items: List<Map<String, Any>>): Boolean {
        return items.isNotEmpty() && items.all { (it["cantidad"] as Int) > 0 && (it["precio_unitario"] as Double) >= 0.0 }
    }

    fun registrar(fechaHora:String, items: List<Map<String, Any>>): Boolean {
        if (!calcularTotal(items)) return false
        val total = items.sumOf { (it["cantidad"] as Int) * (it["precio_unitario"] as Double) }

        val dv = DVenta(context)
        dv.setFechaHora(fechaHora)
        dv.setTotal(total)
        if (!dv.crearVenta()) return false

        val det = DDetalleVenta(context)
        det.setVenta(dv.getId())

        val dp = DProducto(context)

        for (it in items) {
            val idP = it["id_producto"] as Int
            val cant = it["cantidad"] as Int
            val preU = it["precio_unitario"] as Double
            if (!validarItem(idP, cant, preU)) return false

            det.setProducto(idP)
            det.setCantidad(cant)
            det.setPrecioUnitario(preU)
            if (!det.crear()) return false

            dp.setId(idP)
            if (!dp.ajustaStock(-cant)) return false
        }
        return true
    }

}
