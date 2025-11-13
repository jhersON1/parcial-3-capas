package com.example.app.negocio.venta.estrategias

class PricingContext(private var strategy: PricingStrategy) {

    fun setStrategy(strategy: PricingStrategy) {
        this.strategy = strategy
    }


    fun calcularTotal(fechaHora: String, items: List<Map<String, Any>>): Double {
        return strategy.calcular(fechaHora, items)
    }
}
