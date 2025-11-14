package com.example.app.negocio.categoria.command

import com.example.app.negocio.categoria.NCategoria

class ModificarCategoriaCommand(
    private val nCategoria: NCategoria,
    private val id: Int,
    private val nuevoNombre: String
) : CategoriaCommand {

    override fun execute(): CommandResult {
        return try {
            val ok = nCategoria.modificar(id, nuevoNombre)
            if (ok) CommandResult(true, "Categoría modificada")
            else CommandResult(false, "No se pudo modificar")
        } catch (e: Exception) {
            CommandResult(false, e.message ?: "Error inesperado")
        }
    }
}
