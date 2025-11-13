package com.example.app.negocio.venta.estrategias

interface PricingStrategy {
    fun calcular(fechaHora: String, items: List<Map<String, Any>>): Double
}
