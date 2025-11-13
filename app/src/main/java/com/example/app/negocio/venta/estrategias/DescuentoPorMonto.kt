package com.example.app.negocio.venta.estrategias

class DescuentoPorMonto(
    val umbral: Double,
    val porcentaje: Double
) : PricingStrategy {

    init {
        require(umbral > 0) { "El umbral debe ser mayor a 0" }
        require(porcentaje in 0.0..100.0) { "El porcentaje debe estar entre 0 y 100" }
    }

    override fun calcular(fechaHora: String, items: List<Map<String, Any>>): Double {
        val subtotal = items.sumOf {
            val cantidad = it["cantidad"] as Int
            val precio = it["precio_unitario"] as Double
            cantidad * precio
        }

        return if (subtotal >= umbral) {
            val descuento = subtotal * (porcentaje / 100.0)
            subtotal - descuento
        } else {
            subtotal
        }
    }
}
