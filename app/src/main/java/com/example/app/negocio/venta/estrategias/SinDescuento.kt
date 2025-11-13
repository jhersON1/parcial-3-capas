package com.example.app.negocio.venta.estrategias

class SinDescuento : PricingStrategy {

    override fun calcular(fechaHora: String, items: List<Map<String, Any>>): Double {
        return items.sumOf {
            val cantidad = it["cantidad"] as Int
            val precio = it["precio_unitario"] as Double
            cantidad * precio
        }
    }
}
