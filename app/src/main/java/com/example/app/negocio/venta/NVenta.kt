package com.example.app.negocio.venta

import android.content.Context
import com.example.app.datos.venta.DDetalleVenta
import com.example.app.datos.venta.DVenta
import com.example.app.negocio.producto.NProducto

class NVenta(
    context: Context,
    private val nProducto: NProducto
) {
    // Atributos de clase según diagrama UML
    private val dVenta: DVenta = DVenta(context)
    private val dDetalleVenta: DDetalleVenta = DDetalleVenta(context)

    fun validarItem(idProducto:Int, cantidad:Int, precioUnitario:Double): Boolean {
        // Delegar la validación a NProducto
        return nProducto.validarItem(idProducto, cantidad, precioUnitario)
    }

    fun calcularTotal(items: List<Map<String, Any>>): Boolean {
        return items.isNotEmpty() && items.all { (it["cantidad"] as Int) > 0 && (it["precio_unitario"] as Double) >= 0.0 }
    }

    fun registrar(fechaHora:String, items: List<Map<String, Any>>): Boolean {
        if (!calcularTotal(items)) return false
        val total = items.sumOf { (it["cantidad"] as Int) * (it["precio_unitario"] as Double) }

        dVenta.setFechaHora(fechaHora)
        dVenta.setTotal(total)
        if (!dVenta.crearVenta()) return false

        dDetalleVenta.setVenta(dVenta.getId())

        for (it in items) {
            val idP = it["id_producto"] as Int
            val cant = it["cantidad"] as Int
            val preU = it["precio_unitario"] as Double
            if (!validarItem(idP, cant, preU)) return false

            dDetalleVenta.setProducto(idP)
            dDetalleVenta.setCantidad(cant)
            dDetalleVenta.setPrecioUnitario(preU)
            if (!dDetalleVenta.crear()) return false

            // Delegar ajuste de stock a NProducto (no más DProducto aquí)
            if (!nProducto.descontarStock(idP, cant)) return false
        }
        return true
    }

}
