package com.example.app.datos.reporte

import android.content.Context
import com.example.app.datos.base.Conexion

class DReporte(context: Context) {
    private val bd: Conexion = Conexion.instancia(context)

    fun obtenerNumeroVentas(desde:String, hasta:String): Int {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            "SELECT COUNT(*) FROM venta WHERE date(fechaHora) BETWEEN date(?) AND date(?)",
            arrayOf(desde, hasta)
        )
        cur.use { return if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun obtenerTotalVendido(desde:String, hasta:String): Double {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            """
            SELECT IFNULL(SUM(dv.cantidad * dv.precio_unitario),0)
            FROM detalle_venta dv
            JOIN venta v ON v.id = dv.id_venta
            WHERE date(v.fechaHora) BETWEEN date(?) AND date(?)
            """.trimIndent(),
            arrayOf(desde, hasta)
        )
        cur.use { return if (it.moveToFirst()) it.getDouble(0) else 0.0 }
    }

    fun obtenerVentasPorProducto(desde:String, hasta:String): List<Map<String, Any?>> {
        val db = bd.readableDatabase
        val cur = db.rawQuery(
            """
            SELECT p.id, p.nombre, IFNULL(SUM(dv.cantidad),0) as unidades, 
                   IFNULL(SUM(dv.cantidad*dv.precio_unitario),0) as total
            FROM producto p
            LEFT JOIN detalle_venta dv ON dv.id_producto = p.id
            LEFT JOIN venta v ON v.id = dv.id_venta AND date(v.fechaHora) BETWEEN date(?) AND date(?)
            GROUP BY p.id, p.nombre
            ORDER BY total DESC
            """.trimIndent(),
            arrayOf(desde, hasta)
        )
        val lista = mutableListOf<Map<String, Any?>>()
        cur.use {
            while (it.moveToNext()) {
                lista.add(
                    mapOf(
                        "id" to it.getInt(0),
                        "nombre" to it.getString(1),
                        "unidades" to it.getInt(2),
                        "total" to it.getDouble(3)
                    )
                )
            }
        }
        return lista
    }
}

