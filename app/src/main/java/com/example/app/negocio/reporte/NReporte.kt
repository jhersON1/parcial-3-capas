package com.example.app.negocio.reporte

import android.content.Context
import com.example.app.datos.reporte.DReporte

class NReporte(context: Context) {
    private val d = DReporte(context)

    fun obtenerNumeroVentas(desde:String, hasta:String): Int = d.obtenerNumeroVentas(desde, hasta)
    fun obtenerTotalVendido(desde:String, hasta:String): Double = d.obtenerTotalVendido(desde, hasta)
    fun obtenerVentasPorProducto(desde:String, hasta:String): List<Map<String, Any?>> = d.obtenerVentasPorProducto(desde, hasta)
}

