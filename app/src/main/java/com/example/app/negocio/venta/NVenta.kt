package com.example.app.negocio.venta

import android.content.Context
import com.example.app.datos.venta.DDetalleVenta
import com.example.app.datos.venta.DVenta
import com.example.app.negocio.producto.NProducto
import com.example.app.negocio.venta.estrategias.*

class NVenta(
    context: Context,
    private val nProducto: NProducto
) {
    private val dVenta: DVenta = DVenta(context)
    private val dDetalleVenta: DDetalleVenta = DDetalleVenta(context)
    private val pricingContext: PricingContext = PricingContext(SinDescuento())

    /**
     * Selecciona la estrategia por nombre (equivalente a seleccionarEstrategia de FormularioVenta)
     */
    fun seleccionarEstrategia(nombre: String) {
        when (nombre) {
            "Sin Descuento" -> {
                pricingContext.setStrategy(SinDescuento())
            }
            "Descuento VIP" -> {
                pricingContext.setStrategy(DescuentoVIP(15.0))
            }
            "Descuento por Monto" -> {
                pricingContext.setStrategy(DescuentoPorMonto(500.0, 10.0))
            }
            else -> {
                pricingContext.setStrategy(SinDescuento())
            }
        }
    }

    /**
     * Calcula el total con descuento aplicando la estrategia actual
     */
    fun calcularTotalConDescuento(fechaHora: String, items: List<Map<String, Any>>): Double {
        return pricingContext.calcularTotal(fechaHora, items)
    }

    fun validarItem(idProducto:Int, cantidad:Int, precioUnitario:Double): Boolean {
        // Delegar la validación a NProducto
        return nProducto.validarItem(idProducto, cantidad, precioUnitario)
    }

    fun calcularTotal(items: List<Map<String, Any>>): Boolean {
        return items.isNotEmpty() && items.all { (it["cantidad"] as Int) > 0 && (it["precio_unitario"] as Double) >= 0.0 }
    }

    fun registrar(fechaHora:String, items: List<Map<String, Any>>): Boolean {
        if (!calcularTotal(items)) return false

        // Calcular el total usando PricingContext con la estrategia actual
        val total = pricingContext.calcularTotal(fechaHora, items)

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
