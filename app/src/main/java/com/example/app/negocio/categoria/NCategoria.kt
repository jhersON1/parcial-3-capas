package com.example.app.negocio.categoria

import android.content.Context
import com.example.app.datos.categoria.DCategoria

class NCategoria(context: Context) {
    private val dCategoria = DCategoria(context)

    fun registrar(id:Int, nombre:String): Boolean {
        require(nombre.isNotBlank()) { "Nombre requerido" }
        dCategoria.setId(id); dCategoria.setNombre(nombre)
        return dCategoria.crear()
    }

    fun modificar(id:Int, nombre:String): Boolean {
        require(nombre.isNotBlank())
        dCategoria.setId(id); dCategoria.setNombre(nombre)
        val ok = dCategoria.editar()
        return if (!ok && !dCategoria.existeCategoria(id)) {
            // Fallback de demo: si no existe el ID, registrar como nuevo
            dCategoria.setId(0)
            dCategoria.crear()
        } else ok
    }

    fun eliminar(id:Int): Boolean {
        if (dCategoria.existeProductosEnCategoria(id)) return false
        dCategoria.setId(id)
        return dCategoria.eliminar()
    }

    fun getLista(filtro:String): List<Map<String, Any?>> = dCategoria.listar(filtro)
    fun getTabla(filtro:String): List<Map<String, Any?>> = dCategoria.lista(filtro)

    fun existe(id:Int): Boolean = dCategoria.existeCategoria(id)
}
