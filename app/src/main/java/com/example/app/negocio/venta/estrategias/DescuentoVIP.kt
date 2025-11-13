package com.example.app.negocio.venta.estrategias

class DescuentoVIP(private val porcentaje: Double) : PricingStrategy {

    init {
        require(porcentaje in 0.0..100.0) { "El porcentaje debe estar entre 0 y 100" }
    }

    override fun calcular(fechaHora: String, items: List<Map<String, Any>>): Double {
        val subtotal = items.sumOf {
            val cantidad = it["cantidad"] as Int
            val precio = it["precio_unitario"] as Double
            cantidad * precio
        }
        val descuento = subtotal * (porcentaje / 100.0)
        return subtotal - descuento
    }
}
