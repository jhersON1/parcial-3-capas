package com.example.app.negocio.categoria.command

import com.example.app.negocio.categoria.NCategoria

class EliminarCategoriaCommand(
    private val nCategoria: NCategoria,
    private val id: Int
) : CategoriaCommand {

    override fun execute(): CommandResult {
        return try {
            val ok = nCategoria.eliminar(id)
            if (ok) CommandResult(true, "Categoría eliminada")
            else CommandResult(false, "No se pudo eliminar (puede tener productos)")
        } catch (e: Exception) {
            CommandResult(false, e.message ?: "Error inesperado")
        }
    }
}
