package com.example.app.negocio.compra

import android.content.Context
import com.example.app.datos.compra.DCompra
import com.example.app.datos.compra.DDetalleCompra
import com.example.app.datos.producto.DProducto

class NCompra(private val context: Context) {

    fun validarItem(idProducto:Int, cantidad:Int, costoUnitario:Double): Boolean {
        return idProducto > 0 && cantidad > 0 && costoUnitario >= 0
    }

    fun registrar(fechaHora:String, items: List<Map<String, Any>>): Boolean {
        if (items.isEmpty()) return false

        val dc = DCompra(context)
        dc.setFechaHora(fechaHora)
        if (!dc.crearCompra()) return false

        val dd = DDetalleCompra(context)
        dd.setIdCompra(dc.getId())

        val dp = DProducto(context)

        for (it in items) {
            val idP = it["id_producto"] as Int
            val cant = it["cantidad"] as Int
            val cu = it["costo_unitario"] as Double
            if (!validarItem(idP, cant, cu)) return false

            dd.setProducto(idP); dd.setCantidad(cant); dd.setCostoUnitario(cu)
            if (!dd.crear()) return false

            dp.setId(idP)
            if (!dp.ajustaStock(+cant)) return false
        }
        return true
    }

    // Alias 1:1 con el diagrama
    fun registra(fechaHora:String, items: List<Map<String, Any>>): Boolean = registrar(fechaHora, items)
}
