package com.example.app.negocio.producto

import android.content.Context
import com.example.app.datos.producto.DProducto

/**
 * NProducto según diagrama 1:1:
 * + eliminar(int): boolean
 * + getLista(string): List
 * + modificar(int, string, string, double, int): boolean
 * + registrar(int, string, string, double, int): boolean
 */
class NProducto(context: Context) {
    private val dProducto = DProducto(context)

    // Métodos exactos del diagrama
    fun registrar(id: Int, nombre: String, descripcion: String, precio: Double, stock: Int): Boolean {
        require(nombre.isNotBlank()) { "Nombre requerido" }
        require(precio >= 0) { "Precio inválido" }
        require(stock >= 0) { "Stock inválido" }

        dProducto.setId(id)
        dProducto.setNombre(nombre)
        dProducto.setDescripcion(descripcion)
        dProducto.setPrecio(precio)
        dProducto.setStock(stock)
        // Nota: categoriaId no está en diagrama pero necesario para BD
        dProducto.setCategoriaId(1) // categoria por defecto
        return dProducto.crear()
    }

    fun eliminar(id: Int): Boolean {
        dProducto.setId(id)
        // Para la demo: si tiene movimientos, elimina también los detalles asociados
        return if (dProducto.existeMovimientos(id)) dProducto.eliminarForzado() else dProducto.eliminar()
    }
    
    fun getLista(filtro: String): List<Map<String, Any?>> = dProducto.listar(filtro)

    // --- Métodos de compatibilidad para P que necesita categoriaId ---
    fun registra(id: Int, nombre: String, descripcion: String, precio: Double, stock: Int, categoriaId: Int): Boolean {
        dProducto.setId(id)
        dProducto.setNombre(nombre)
        dProducto.setDescripcion(descripcion)
        dProducto.setPrecio(precio)
        dProducto.setStock(stock)
        dProducto.setCategoriaId(categoriaId)
        return dProducto.crear()
    }

    fun modifica(id: Int, nombre: String, descripcion: String, precio: Double, stock: Int, categoriaId: Int): Boolean {
        dProducto.setId(id)
        dProducto.setNombre(nombre)
        dProducto.setDescripcion(descripcion)
        dProducto.setPrecio(precio)
        dProducto.setStock(stock)
        dProducto.setCategoriaId(categoriaId)
        return dProducto.editar()
    }

    // --- Métodos para validación y ajuste de stock (usados por NVenta) ---
    fun validarItem(idProducto: Int, cantidad: Int, precioUnitario: Double): Boolean {
        if (idProducto <= 0 || cantidad <= 0 || precioUnitario < 0) return false
        // Verificar que el producto exista y tenga stock suficiente
        dProducto.setId(idProducto)
        val productos = dProducto.listar("")
        val producto = productos.find { (it["id"] as? Int) == idProducto } ?: return false
        val stockActual = producto["stock"] as? Int ?: 0
        return stockActual >= cantidad
    }

    fun descontarStock(idProducto: Int, cantidad: Int): Boolean {
        dProducto.setId(idProducto)
        return dProducto.ajustaStock(-cantidad)
    }
}
